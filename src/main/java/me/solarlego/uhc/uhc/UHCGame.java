package me.solarlego.uhc.uhc;

import me.solarlego.solarmain.FileUtils;
import me.solarlego.solarmain.Stats;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.uhc.UHC;
import me.solarlego.uhc.commands.CommandGame;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import me.solarlego.uhc.Scoreboard;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class UHCGame {

    public final HashMap<ItemStack, Material[]> crafts = new CustomCrafts(this).getCrafts();
    public HashMap<UUID, PlayerInfo> players = new HashMap<>();
    public int time = -638;
    public World worldUHC;
    public World worldNether;
    public boolean isRunning = false;

    private final Scoreboard sb = new Scoreboard(this);
    private Timer timer = new Timer();

    public UHCGame(String overworld, String nether) {
        Bukkit.getServer().getPluginManager().registerEvents(new UHCEvents(this, overworld, nether), UHC.getPlugin());
        Bukkit.getServer().getPluginManager().registerEvents(new CommandGame(this), UHC.getPlugin());
        UHC.getPlugin().addGame(this);

        WorldCreator wc = new WorldCreator(overworld);
        try {
            Field biomesField = BiomeBase.class.getDeclaredField("biomes");
            biomesField.setAccessible(true);
            if (biomesField.get(null) instanceof BiomeBase[]) {
                BiomeBase[] biomes = (BiomeBase[]) biomesField.get(null);
                biomes[BiomeBase.DEEP_OCEAN.id] = BiomeBase.PLAINS;
                biomes[BiomeBase.OCEAN.id] = BiomeBase.FOREST;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        wc.generatorSettings("{\"coordinateScale\":684.412,\"heightScale\":684.412,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":80.0,\"mainNoiseScaleY\":160.0,\"mainNoiseScaleZ\":80.0,\"baseSize\":8.5,\"stretchY\":12.0,\"biomeDepthWeight\":1.0,\"biomeDepthOffset\":0.0,\"biomeScaleWeight\":1.0,\"biomeScaleOffset\":0.0,\"seaLevel\":63,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":8,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":4,\"useLavaLakes\":true,\"lavaLakeChance\":99,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":3,\"riverSize\":4,\"dirtSize\":33,\"dirtCount\":0,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":33,\"graniteCount\":0,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,\"dioriteSize\":33,\"dioriteCount\":0,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,\"andesiteSize\":33,\"andesiteCount\":0,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,\"coalSize\":17,\"coalCount\":24,\"coalMinHeight\":0,\"coalMaxHeight\":128,\"ironSize\":9,\"ironCount\":24,\"ironMinHeight\":0,\"ironMaxHeight\":64,\"goldSize\":9,\"goldCount\":3,\"goldMinHeight\":0,\"goldMaxHeight\":32,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,\"diamondSize\":8,\"diamondCount\":2,\"diamondMinHeight\":0,\"diamondMaxHeight\":16,\"lapisSize\":7,\"lapisCount\":1,\"lapisCenterHeight\":16,\"lapisSpread\":16}");
        worldUHC = wc.createWorld();
        worldNether = new WorldCreator(nether).environment(World.Environment.NETHER).createWorld();

        for (int x = -10; x < 11; x++) {
            for (int z = -10; z < 11; z++) {
                worldUHC.getBlockAt(x, 200, z).setType(Material.BARRIER);
                worldUHC.getBlockAt(x, 210, z).setType(Material.BARRIER);
                if (Math.abs(x) == 10 && Math.abs(z) == 10) {
                    for (int y = 200; y < 211; y++) {
                        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) worldUHC).getHandle();
                        nmsWorld.setTypeAndData(new BlockPosition(x, y, z), net.minecraft.server.v1_8_R3.Block.getByCombinedId(160 + (3 << 12)), 3);
                    }
                } else if (Math.abs(x) == 10 || Math.abs(z) == 10) {
                    for (int y = 201; y < 210; y++) {
                        worldUHC.getBlockAt(x, y, z).setType(Material.BARRIER);
                    }
                }
            }
        }
        worldUHC.setSpawnLocation(0, 201, 0);
        worldUHC.setGameRuleValue("doDaylightCycle", "false");
        worldUHC.setGameRuleValue("naturalRegeneration", "false");
        worldNether.setGameRuleValue("naturalRegeneration", "false");
        worldUHC.setDifficulty(Difficulty.HARD);
        worldNether.setDifficulty(Difficulty.HARD);
        worldUHC.getWorldBorder().setSize(2000);
        worldUHC.getWorldBorder().setCenter(0, 0);
        worldNether.getWorldBorder().setSize(250);
        worldNether.getWorldBorder().setCenter(0, 0);
        worldUHC.setMonsterSpawnLimit(worldUHC.getMonsterSpawnLimit() / 2);

    }

    public void playerJoin(Player player) {
        if (!players.containsKey(player.getUniqueId())) {
            players.put(player.getUniqueId(), new PlayerInfo(player));
        }
        PlayerInfo pInfo = players.get(player.getUniqueId());
        if (time < 0) {
            player.teleport(worldUHC.getSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setPlayerListName(Stats.get(player.getUniqueId()).getPrefix() + "\u00A7c" + player.getName());
            player.getInventory().setItem(2, Hub.createItemStack(Material.WOOL, "\u00A7fTeam Selector", 14, "\u00A7eRight Click to Open!"));
            player.getInventory().setItem(4, Hub.createItemStack(Material.STONE_PICKAXE, "\u00A7fKit Selector", 0, "\u00A7eRight Click to Open!"));
            ItemStack recipes = Hub.createItemStack(Material.BOOK, "\u00A7fRecepies", 0, "\u00A7eRight Click to Open!");
            recipes.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            ItemMeta meta = recipes.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            recipes.setItemMeta(meta);
            player.getInventory().setItem(6, recipes);
            for (PlayerInfo playerInfo : players.values()) {
                playerInfo.getPlayer().sendMessage(Stats.get(player.getUniqueId()).getColor() + player.getName() + " \u00A7ehas joined (\u00A7b" + players.size() + "\u00A7e)");
                sb.updateScoreboard(playerInfo.getPlayer());
            }
            if (players.size() == 2) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        runTimer();
                    }
                }, 0, 1000);
            }
        } else if (pInfo.getDummy() != null) {
            if (pInfo.isDead()) {
                pInfo.getDummy().replace(player);
                playerDeath(player);
            } else {
                pInfo.getDummy().rejoin(player, time);
                for (PlayerInfo playerInfo : players.values()) {
                    if (checkWorld(pInfo.getPlayer().getWorld())) {
                        if (playerInfo.isDead()) {
                            player.hidePlayer(playerInfo.getPlayer());
                        } else {
                            player.showPlayer(playerInfo.getPlayer());
                        }
                    }
                }
            }
            player.setPlayerListName(Stats.get(player.getUniqueId()).getPrefix() + "\u00A7" + pInfo.getTeamColor() + player.getName());
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            pInfo.setDead(true);
            pInfo.setTeam("Spectator");
            for (PlayerInfo playerInfo : players.values()) {
                if (!playerInfo.isDead()) {
                    playerInfo.getPlayer().hidePlayer(player);
                }
            }
            player.setPlayerListName(Stats.get(player.getUniqueId()).getPrefix() + player.getName());
        }
    }

    private void runTimer() {
        Bukkit.getServer().getScheduler().runTask(UHC.getPlugin(), () -> {
            switch (time) {
                case -60:
                case -30:
                case -15:
                case -10:
                case -5:
                case -4:
                case -3:
                case -2:
                case -1:
                    for (PlayerInfo pInfo : players.values()) {
                        pInfo.getPlayer().sendMessage("\u00A7eThe game will start in \u00A7" + (time > -10 ? "c" : "6") + -time + " \u00A7eseconds!");
                        pInfo.getPlayer().playSound(pInfo.getPlayer().getLocation(), Sound.CLICK, 1, 1);
                    }
                    break;
                case 0:
                    ArrayList<String> present = new ArrayList<>();
                    for (PlayerInfo pInfo : players.values()) {
                        if (!present.contains(pInfo.getTeam())) {
                            present.add(pInfo.getTeam());
                        }
                    }
                    if (present.size() > 1) {
                        start();
                    } else {
                        for (PlayerInfo pInfo : players.values()) {
                            pInfo.getPlayer().sendMessage("\u00A7cNot enough teams. Start canceled.");
                            pInfo.getPlayer().playSound(pInfo.getPlayer().getLocation(), Sound.CLICK, 1, 1);
                        }
                        time -= 15;
                    }
                    break;
                case 600:
                    for (PlayerInfo pInfo : players.values()) {
                        if (!pInfo.isDead() && checkWorld(pInfo.getPlayer().getWorld())) {
                            pInfo.getPlayer().sendMessage("\u00A7fYou have been \u00A7cHealed\u00A7f.");
                            pInfo.getPlayer().setHealth(40);
                            pInfo.getPlayer().sendMessage("\u00A76+50 coins! (Survived)");
                            pInfo.addCoins(50);
                        }
                    }
                    break;
                case 900:
                    worldUHC.getWorldBorder().setSize(400, 900);
                    worldNether.getWorldBorder().setSize(50, 900);
                    for (PlayerInfo pInfo : players.values()) {
                        if (checkWorld(pInfo.getPlayer().getWorld())) {
                            pInfo.getPlayer().sendMessage("\u00A7cPvP \u00A7fis now \u00A7aEnabled\u00A7f. The \u00A79worldborder \u00A7fwill begin to shrink.");
                            pInfo.getPlayer().playSound(pInfo.getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                            if (!pInfo.isDead()) {
                                pInfo.getPlayer().sendMessage("\u00A76+50 coins! (Survived)");
                                pInfo.addCoins(50);
                            }
                        }
                    }
                    break;
                case 2100:
                    for (int x = -20; x < 21; x++) {
                        for (int z = -20; z < 21; z++) {
                            worldUHC.getBlockAt(x, 200, z).setType(Material.SMOOTH_BRICK);
                            worldUHC.getBlockAt(x, 220, z).setType(Material.STAINED_GLASS);
                            if (Math.abs(x) == 20 || Math.abs(z) == 20) {
                                for (int y = 201; y < 220; y++) {
                                    worldUHC.getBlockAt(x, y, z).setType(Material.STAINED_GLASS);
                                }
                            }
                        }
                    }
                    for (PlayerInfo pInfo : players.values()) {
                        if (checkWorld(pInfo.getPlayer().getWorld())) {
                            pInfo.getPlayer().teleport(new Location(worldUHC, 0, 201, 0));
                            pInfo.getPlayer().sendMessage("\u00A77Teleporting you to Deathmatch...");
                        }
                    }
                    break;
                case 2115:
                    for (PlayerInfo pInfo : players.values()) {
                        if (checkWorld(pInfo.getPlayer().getWorld())) {
                            pInfo.getPlayer().sendMessage("\u00A7fPvP is \u00A7aEnabled.");
                        }
                    }
                    break;
            }
            if (time >= 1200 && time % 60 == 0) {
                for (PlayerInfo pInfo : players.values()) {
                    if (pInfo.getPlayer().getLocation().getBlockY() < 33 && checkWorld(pInfo.getPlayer().getWorld())) {
                        double newHealth = Math.max(pInfo.getPlayer().getHealth() - 4, 1);
                        pInfo.getPlayer().damage(pInfo.getPlayer().getHealth() - newHealth);
                    }
                }
            }
            for (PlayerInfo pInfo : players.values()) {
                sb.updateScoreboard(pInfo.getPlayer());
            }
            time++;
        });
    }

    private void start() {
        isRunning = true;
        timer.cancel();
        for (PlayerInfo pInfo : players.values()) {
            pInfo.getPlayer().playSound(pInfo.getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
            pInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0));
            pInfo.getPlayer().getInventory().clear();
            pInfo.getPlayer().closeInventory();
        }

        HashMap<String, int[]> teamLocs = new HashMap<>();
        for (String team : new String[] {"Red", "Gold", "Green", "Aqua", "Blue", "Purple", "Black"}) {
            int[] loc = new int[] { new Random().nextInt(2000) - 1000, new Random().nextInt(2000) - 1000 };
            teamLocs.put(team, loc);
            for (int x = 0; x < 17; x++) {
                for (int z = 0; z < 17; z++) {
                    int finalX = (int) Math.floor(x - 8 + loc[0] / 16.0);
                    int finalZ = (int) Math.floor(z - 8 + loc[1] / 16.0);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> {
                        if (!worldUHC.isChunkLoaded(finalX, finalZ)) {
                            worldUHC.getChunkAt(finalX, finalZ);
                        }
                    }, 17 * x + z);
                }
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> {
            for (PlayerInfo pInfo : players.values()) {
                int[] loc = teamLocs.get(pInfo.getTeam());
                pInfo.getPlayer().teleport(worldUHC.getHighestBlockAt(loc[0], loc[1]).getLocation().add(0.5, 1, 0.5));
            }
            for (int x = -10; x < 11; x++) {
                for (int z = -10; z < 11; z++) {
                    worldUHC.getBlockAt(x, 200, z).setType(Material.AIR);
                    worldUHC.getBlockAt(x, 210, z).setType(Material.AIR);
                    if (Math.abs(x) == 10 || Math.abs(z) == 10) {
                        for (int y = 201; y < 210; y++) {
                            worldUHC.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    }
                }
            }
        }, 290L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> {
            for (PlayerInfo pInfo : players.values()) {
                pInfo.getPlayer().setGameMode(GameMode.SURVIVAL);
                pInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 18000, 2));
                switch (pInfo.getKit()) {
                    case "Stone Tools":
                        pInfo.getPlayer().getInventory().setItem(0, new ItemStack(Material.STONE_SWORD, 1));
                        pInfo.getPlayer().getInventory().setItem(1, createItemStack(Material.STONE_PICKAXE, "", 1, 0, new EnchantPair(Enchantment.DIG_SPEED, 1)));
                        pInfo.getPlayer().getInventory().setItem(2, new ItemStack(Material.STONE_AXE, 1));
                        pInfo.getPlayer().getInventory().setItem(3, new ItemStack(Material.STONE_SPADE, 1));
                        break;
                    case "Looter":
                        pInfo.getPlayer().getInventory().setItem(0, createItemStack(Material.IRON_SWORD, "", 1, 0, new EnchantPair(Enchantment.LOOT_BONUS_MOBS, 1)));
                        pInfo.getPlayer().getInventory().setItem(1, new ItemStack(Material.SULPHUR, 3));
                        pInfo.getPlayer().getInventory().setItem(2, new ItemStack(Material.FEATHER, 4));
                        break;
                    case "Archer":
                        pInfo.getPlayer().getInventory().setItem(0, new ItemStack(Material.STRING, 8));
                        pInfo.getPlayer().getInventory().setItem(1, new ItemStack(Material.ARROW, 32));
                        break;
                }
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    runTimer();
                }
            }, 0, 1000);
        }, 300L);
    }

    public void playerKill(Player killer) {
        players.get(killer.getUniqueId()).addKills(1);
        killer.sendMessage("\u00A76+100 coins! (Kill)");
        players.get(killer.getUniqueId()).addCoins(100);
        killer.playSound(killer.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
    }

    public void playerLeave(Player player, Location last) {
        if (time < 0) {
            players.remove(player.getUniqueId());
            if (players.size() < 2) {
                time = -60;
                timer.cancel();
                for (PlayerInfo pInfo : players.values()) {
                    pInfo.getPlayer().sendMessage("\u00A7cNot enough players. Start canceled.");
                    pInfo.getPlayer().playSound(pInfo.getPlayer().getLocation(), Sound.CLICK, 1, 1);
                }
            }
        } else if (!players.get(player.getUniqueId()).isDead()) {
            players.get(player.getUniqueId()).makeDummy(player, last, time);
        }

        for (PlayerInfo pInfo : players.values()) {
            sb.updateScoreboard(pInfo.getPlayer());
        }
    }

    public void playerDeath(Player player) {
        players.get(player.getUniqueId()).setDead(true);
        if (checkWorld(player.getWorld())) {
            player.spigot().respawn();
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().clear();
            for (PlayerInfo pInfo : players.values()) {
                if (checkWorld(pInfo.getPlayer().getWorld())) {
                    if (pInfo.isDead()) {
                        player.showPlayer(pInfo.getPlayer());
                    } else {
                        pInfo.getPlayer().hidePlayer(player);
                    }
                }
            }
        }
    }

    public void checkWin() {
        if (isRunning) {
            String firstVal = "";
            for (PlayerInfo pInfo : players.values()) {
                if (!checkWorld(pInfo.getPlayer().getWorld()) || pInfo.isDead()) {
                    continue;
                }
                if (firstVal.equals("")) {
                    firstVal = pInfo.getTeam();
                } else if (!pInfo.getTeam().equals(firstVal)) {
                    return;
                }
            }
            win(firstVal);
        }
    }

    private void win(String team) {
        timer.cancel();
        isRunning = false;

        StringBuilder startMessage = new StringBuilder("\u00A76\u00A7m----------------------------------\n\u00A7fWinner: ");
        int i = 0;
        for (PlayerInfo pInfo : players.values()) {
            if (pInfo.getTeam().equals(team)) {
                if (i % 2 == 0 && i != 0) {
                    startMessage.toString().concat("\n  ");
                }
                startMessage.append(Stats.get(pInfo.getPlayer().getUniqueId()).getPrefix()).append(pInfo.getPlayer().getName()).append(", ");
                i++;
                if (checkWorld(pInfo.getPlayer().getWorld())) {
                    pInfo.getPlayer().sendMessage("\u00A76+200 coins! (Win)");
                    pInfo.addCoins(200);
                    sendTitle(pInfo.getPlayer(), "\u00A76VICTORY!");
                }
            } else {
                if (checkWorld(pInfo.getPlayer().getWorld())) {
                    sendTitle(pInfo.getPlayer(), "\u00A7cDefeat");
                }
            }
        }
        startMessage = new StringBuilder(startMessage.substring(0, startMessage.length() - 2));
        for (PlayerInfo pInfo : players.values()) {
            if (checkWorld(pInfo.getPlayer().getWorld())) {
                pInfo.getPlayer().sendMessage(startMessage.toString()
                        .concat("\n\n\u00A7fKills: \u00A74")
                        .concat(String.valueOf(pInfo.getKills()))
                        .concat("\n\u00A7fCoins: \u00A76")
                        .concat(String.valueOf(pInfo.getCoins()))
                        .concat("\n\u00A76\u00A7m----------------------------------"));
            }
            UHC.getPlugin().updatePlayerFile(pInfo.getPlayer().getUniqueId() + ".coins", pInfo.getCoins());
            UHC.getPlugin().updatePlayerFile(pInfo.getPlayer().getUniqueId() + ".kills", pInfo.getKills());
            if (pInfo.getTeam().equals(team)) {
                UHC.getPlugin().updatePlayerFile(pInfo.getPlayer().getUniqueId() + ".wins", 1);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), this::shutdown, 1200);
    }

    public ItemStack createItemStack(Material material, String name, Integer amount, Integer damage, EnchantPair... enchants) {
        ItemStack item = new ItemStack(material, amount, damage.shortValue());

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).damage(damage);
        }
        if (!name.equals("")) {
            meta.setDisplayName(name);
        }
        for (EnchantPair pair : enchants) {
            meta.addEnchant(pair.getEnchantment(), pair.getLevel(), true);
        }
        item.setItemMeta(meta);

        return item;
    }

    private void sendTitle(Player player, String title) {
        CraftPlayer craftplayer = (CraftPlayer) player;
        PlayerConnection connection = craftplayer.getHandle().playerConnection;
        ChatComponentText titleJSON = new ChatComponentText(title);
        ChatComponentText subtitleJSON = new ChatComponentText("");
        PacketPlayOutTitle duraPacket = new PacketPlayOutTitle(5, 65, 10);
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleJSON);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleJSON);
        connection.sendPacket(duraPacket);
        connection.sendPacket(titlePacket);
        connection.sendPacket(subtitlePacket);
    }

    public boolean checkWorld(World world) {
        return world == worldUHC || world == worldNether;
    }

    public String getVar(Player player, String name) {
        switch (name) {
            case "time":
                if (time >= 0) {
                    return (int) Math.floor((double) time / 60) + ":" + (String.valueOf(time % 60).length() == 1 ? "0" : "") + time % 60;
                } else if (players.size() < 2) {
                    return "Waiting";
                } else {
                    return -time + "s";
                }
            case "players":
                return Integer.toString(players.size());
            case "kills":
                if (players.containsKey(player.getUniqueId())) {
                    return Integer.toString(players.get(player.getUniqueId()).getKills());
                } else {
                    return "0";
                }
            case "nextevent":
                if (time < 0) {
                    return "Start";
                } else if (time < 1) {
                    return "Starting";
                } else if (time < 600) {
                    return "Final Heal";
                } else if (time < 900) {
                    return "PvP Start";
                } else if (time < 1200) {
                    return "Damage Start";
                } else if (time < 1800) {
                    return "Border Done";
                } else if (time < 2100) {
                    return "Deathmatch";
                } else if (time < 2115) {
                    return "PVP Start";
                } else {
                    return "None";
                }
            case "nexttime":
                int tempTime = 0;
                if (time < 1) {
                    tempTime = -time;
                } else if (time < 600) {
                    tempTime = 600 - time;
                } else if (time < 900) {
                    tempTime = 900 - time;
                } else if (time < 1200) {
                    tempTime = 1200 - time;
                } else if (time < 1800) {
                    tempTime = 1800 - time;
                } else if (time < 2100) {
                    tempTime = 2100 - time;
                } else if (time < 2115) {
                    tempTime = 2115 - time;
                }
                return (int) Math.floor((double) tempTime / 60) + ":" + (String.valueOf(tempTime % 60).length() == 1 ? "0" : "") + tempTime % 60;
        }
        return "";
    }

    public void shutdown() {
        isRunning = false;
        for (Player player : worldUHC.getPlayers()) {
            Hub.sendHub(player);
        }
        for (Player player : worldNether.getPlayers()) {
            Hub.sendHub(player);
        }
        Bukkit.getServer().unloadWorld(worldUHC, false);
        Bukkit.getServer().unloadWorld(worldNether, false);
        FileUtils.deleteDirectory(new File("./" + worldUHC.getName()));
        FileUtils.deleteDirectory(new File("./" + worldNether.getName()));
        timer.cancel();
    }

}

