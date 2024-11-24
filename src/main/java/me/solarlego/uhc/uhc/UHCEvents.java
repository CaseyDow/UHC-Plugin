package me.solarlego.uhc.uhc;

import me.solarlego.solarmain.Stats;
import me.solarlego.solarmain.commands.CommandChat;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.uhc.UHC;
import me.solarlego.uhc.gui.RecipesGUI;
import me.solarlego.uhc.gui.TeamGUI;
import me.solarlego.uhc.gui.kits.KitsGUI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UHCEvents implements Listener {

    private final UHCGame game;
    private final String overworld;
    private final String nether;

    public UHCEvents(UHCGame uhc, String overworld, String nether) {
        game = uhc;
        this.overworld = overworld;
        this.nether = nether;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (event.getWorld().getName().equals(overworld) || event.getWorld().getName().equals(nether)) {
            event.getWorld().setKeepSpawnInMemory(false);
            for (int x = 0; x < 17; x++) {
                for (int z = 0; z < 17; z++) {
                    int finalX = x - 8;
                    int finalZ = z - 8;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> {
                        event.getWorld().getChunkAt(finalX, finalZ);
                        game.time++;
                    }, (17 * x + z) / 2L);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld()) && !game.checkWorld(event.getFrom())) {
            Hub.resetPlayer(event.getPlayer());
            event.getPlayer().setMaxHealth(40);
            event.getPlayer().setHealth(40);

            FileConfiguration playersFile = YamlConfiguration.loadConfiguration(new File(UHC.getPlugin().getDataFolder(), "players.yml"));
            playersFile.addDefault(event.getPlayer().getUniqueId() + ".coins", 0);
            playersFile.addDefault(event.getPlayer().getUniqueId() + ".kills", 0);
            playersFile.addDefault(event.getPlayer().getUniqueId() + ".wins", 0);
            playersFile.options().copyDefaults(true);
            try {
                playersFile.save(new File(UHC.getPlugin().getDataFolder(), "players.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            game.playerJoin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (game.checkWorld(event.getFrom().getWorld()) && !game.checkWorld(event.getTo().getWorld())) {
            game.playerLeave(event.getPlayer(), event.getFrom().add(0, 0.1, 0));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (game.time < 5 || game.time >= 2100 && game.time < 2115) {
                event.setCancelled(true);
            } else if (event.getEntity() instanceof Player) {
                if ((event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE) && game.time <= 900) {
                    event.setCancelled(true);
                } else if (event instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                    if (damager instanceof Player) {
                        String hitTeam = game.players.get(event.getEntity().getUniqueId()).getTeam();
                        String damagerTeam = game.players.get(damager.getUniqueId()).getTeam();
                        if (hitTeam.equals(damagerTeam) || game.time <= 900) {
                            event.setCancelled(true);
                        }
                    }
                }
            } else if (event.getEntity() instanceof Zombie) {
                for (PlayerInfo pInfo : game.players.values()) {
                    if (pInfo.getDummy() != null && pInfo.getDummy().getUUID() == event.getEntity().getUniqueId()) {
                        if (game.time <= 900) {
                            event.setCancelled(true);
                        } else {
                            event.getEntity().setCustomName(event.getEntity().getName().split(" ")[0] + " \u00A7f(" + (int) (((Zombie) event.getEntity()).getHealth() - event.getFinalDamage()) + ")");
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            PlayerInfo dieInfo = game.players.get(event.getEntity().getUniqueId());
            String deathMessage = "\u00A7f" + event.getDeathMessage().replace(dieInfo.getPlayer().getName(), "\u00A7" + dieInfo.getTeamColor() + dieInfo.getPlayer().getName() + "\u00A7f") + "!";
            event.setDeathMessage("");
            if (event.getEntity().getKiller() != null) {
                PlayerInfo killInfo = game.players.get(event.getEntity().getKiller().getUniqueId());
                deathMessage = deathMessage.replace(killInfo.getPlayer().getName(), "\u00A7" + killInfo.getTeamColor() + killInfo.getPlayer().getName() + "\u00A7f");
            }
            for (PlayerInfo pInfo : game.players.values()) {
                if (game.checkWorld(pInfo.getPlayer().getWorld())) {
                    pInfo.getPlayer().sendMessage(deathMessage);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (event.getEntity() instanceof Player || event.getEntity() instanceof Zombie) {
                if (event.getEntity() instanceof Zombie) {
                    boolean found = false;
                    for (PlayerInfo pInfo : game.players.values()) {
                        if (pInfo.getDummy() != null && pInfo.getDummy().getUUID() == event.getEntity().getUniqueId()) {
                            found = true;
                            pInfo.setDead(true);

                            String deathMessage = "\u00A7" + pInfo.getTeamColor() + pInfo.getPlayer().getName() + "\u00A7f died!";
                            if (event.getEntity().getKiller() != null) {
                                PlayerInfo killInfo = game.players.get(event.getEntity().getKiller().getUniqueId());
                                deathMessage = "\u00A7" + pInfo.getTeamColor() + pInfo.getPlayer().getName() + "\u00A7f was slain by \u00A7" + killInfo.getTeamColor() + killInfo.getPlayer().getName() + "\u00A7f!";
                            }
                            for (PlayerInfo info : game.players.values()) {
                                if (game.checkWorld(pInfo.getPlayer().getWorld())) {
                                    info.getPlayer().sendMessage(deathMessage);
                                }
                            }

                            event.setDroppedExp(pInfo.getDummy().getExp());
                            event.getDrops().clear();
                            event.getDrops().addAll(pInfo.getDummy().getItems());
                            break;
                        }
                    }
                    if (found) {
                        event.getEntity().setCustomName(event.getEntity().getName().split(" ")[0]);
                    } else {
                        return;
                    }
                } else {
                    game.playerDeath((Player) event.getEntity());
                    ((Player) event.getEntity()).setBedSpawnLocation(event.getEntity().getLocation(), true);
                }
                if (event.getEntity().getKiller() != null) {
                    game.playerKill(event.getEntity().getKiller());
                    event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                    event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
                    event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0));
                }
                ItemStack playerHead = game.createItemStack(Material.SKULL_ITEM, "\u00A7f" + event.getEntity().getName() + "'s Skull", 1, 3);
                playerHead.getItemMeta().setLore(new ArrayList<>(Arrays.asList("\u00A7cRegeneration III \u00A7ffor 4 seconds", "\u00A7bSpeed I \u00A7ffor 10 seconds", "\u00A7eAbsorption I \u00A7ffor 2 minutes")));
                event.getDrops().add(playerHead);
                game.checkWin();

            } else if (event.getEntity() instanceof Cow) {
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.RAW_BEEF) {
                        event.getDrops().get(event.getDrops().indexOf(drop)).setType(Material.COOKED_BEEF);
                    }
                }
            } else if (event.getEntity() instanceof Pig) {
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.PORK) {
                        event.getDrops().get(event.getDrops().indexOf(drop)).setType(Material.GRILLED_PORK);
                    }
                }
            } else if (event.getEntity() instanceof Chicken) {
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.RAW_CHICKEN) {
                        event.getDrops().get(event.getDrops().indexOf(drop)).setType(Material.COOKED_CHICKEN);
                    }
                }
            } else if (event.getEntity() instanceof Sheep) {
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.MUTTON) {
                        event.getDrops().get(event.getDrops().indexOf(drop)).setType(Material.COOKED_MUTTON);
                    }
                }
            } else if (event.getEntity() instanceof Enderman) {
                if (event.getDrops().isEmpty()) {
                    event.getDrops().add(new ItemStack(Material.ENDER_PEARL));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            game.playerLeave(event.getPlayer(), event.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            ItemStack heldItem = event.getPlayer().getItemInHand();
            if (event.getAction() != Action.PHYSICAL && game.time < 0 && event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
                if (heldItem.getType() == Material.WOOL) {
                    new TeamGUI(game, event.getPlayer());
                } else if (event.getPlayer().getInventory().getHeldItemSlot() == 4 && heldItem.getType() != Material.AIR) {
                    new KitsGUI(game, event.getPlayer());
                } else if (heldItem.getType() == Material.BOOK) {
                    new RecipesGUI(game, event.getPlayer());
                    event.setCancelled(true);
                }
            }
            if (heldItem.getType() == Material.SKULL_ITEM && heldItem.getDurability() == 3) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (heldItem.getAmount() > 1) {
                        heldItem.setAmount(heldItem.getAmount() - 1);
                    } else {
                        event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                    }
                    event.setCancelled(true);
                    if (heldItem.getItemMeta() != null && "\u00A76Golden Head".equals(heldItem.getItemMeta().getDisplayName())) {
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 144, 2));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
                    } else if (heldItem.getItemMeta() != null && "\u00A74Strong Head".equals(heldItem.getItemMeta().getDisplayName())) {
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 0));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 0));
                    } else if (heldItem.getItemMeta() != null && "\u00a7bSpeed Head".equals(heldItem.getItemMeta().getDisplayName())) {
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 96, 2));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1));
                    } else {
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 96, 2));
                        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (game.checkWorld(event.getWhoClicked().getWorld()) && event.getWhoClicked().getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld()) && (event.getPlayer().getGameMode() == GameMode.ADVENTURE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (game.checkWorld(event.getBlock().getWorld())) {
            if (game.time >= 2100) {
                if (Math.abs(event.getBlock().getX()) >= 20 || Math.abs(event.getBlock().getZ()) >= 20 || Math.abs(210 - event.getBlock().getY()) >= 10) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getBlock().getType() == Material.LEAVES || event.getBlock().getType() == Material.LEAVES_2) {
                if ((new Random()).nextInt(32) == 0) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
                }
            } else if (event.getBlock().getType() == Material.GRAVEL) {
                if ((new Random()).nextInt(4) == 0) {
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.FLINT));
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (game.time >= 2100) {
                event.blockList().removeIf(block -> Math.abs(block.getX()) >= 20 || Math.abs(block.getZ()) >= 20 || Math.abs(210 - block.getY()) >= 10);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (game.checkWorld(event.getBlock().getWorld())) {
            if (game.time >= 2100) {
                event.blockList().removeIf(block -> Math.abs(block.getX()) >= 20 || Math.abs(block.getZ()) >= 20 || Math.abs(210 - block.getY()) >= 10);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (game.time <= 900) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUsePortal(PlayerPortalEvent event) {
        if (event.getFrom().getWorld() == game.worldUHC) {
            event.useTravelAgent(true);
            event.setTo(new Location(game.worldNether, (double) event.getFrom().getBlockX() / 8, event.getFrom().getBlockY(), (double) event.getFrom().getBlockZ() / 8));
        } else if (event.getFrom().getWorld() == game.worldNether) {
            event.useTravelAgent(true);
            event.setTo(new Location(game.worldUHC, (double) event.getFrom().getBlockX() * 8, event.getFrom().getBlockY(), (double) event.getFrom().getBlockZ() * 8));
        }
    }

    @EventHandler
    public void onItemBurn(EntityCombustEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (event.getEntity() instanceof Item) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityShear(PlayerShearEntityEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (event.getEntity() instanceof Sheep) {
                if ((new Random()).nextInt(5) == 0) {
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.STRING));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            if (event.getItem().getType() == Material.GOLDEN_APPLE) {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 96, 2));
            }
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (game.checkWorld(event.getView().getPlayer().getWorld())) {
            if (event.getInventory().getResult().getType() == Material.GOLDEN_APPLE && event.getInventory().getResult().getDurability() == 1) {
                event.getInventory().setResult(null);
            }
        } else {
            if (game.crafts.containsKey(event.getRecipe().getResult())) {
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            event.setRespawnLocation(event.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            if (CommandChat.getChat(event.getPlayer()).equals("team")) {
                event.setCancelled(true);
                Stats info = Stats.get(event.getPlayer().getUniqueId());
                PlayerInfo sInfo = game.players.get(event.getPlayer().getUniqueId());
                String msg = "\u00A7" + sInfo.getTeamColor() + "[" + sInfo.getTeam() + "] \u00A7f" + info.getPrefix() + event.getPlayer().getName() + ": " + event.getMessage();
                for (PlayerInfo pInfo : game.players.values()) {
                    if (pInfo.getTeam().equals(sInfo.getTeam()) && game.checkWorld(pInfo.getPlayer().getWorld())) {
                        pInfo.getPlayer().sendMessage(msg);
                    }
                }
            }
        }
    }

}
