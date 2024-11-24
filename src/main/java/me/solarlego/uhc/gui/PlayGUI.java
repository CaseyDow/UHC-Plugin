package me.solarlego.uhc.gui;

import me.solarlego.solarmain.Party;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.uhc.UHC;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PlayGUI implements Listener {

    private final Inventory inv;

    public PlayGUI(Player player) {
        inv = Bukkit.createInventory(null, 36, "UHC");
        initializeItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems() {
        ItemStack blank = Hub.createItemStack(Material.STAINED_GLASS_PANE, " ", 15);
        for (int i = 0; i < 10; i++) {
            inv.setItem(i, blank);
        }
        inv.setItem(11, blank);
        for (int i = 17; i < 36; i++) {
            inv.setItem(i, blank);
        }
        inv.setItem(10, Hub.createItemStack(Material.GOLDEN_APPLE, "\u00A7fNew UHC", 0, "\u00A77Create a New Game"));
        int pos = 12;
        for (UHCGame game : UHC.getPlugin().getGames()) {
            if (pos < 17 && Bukkit.getWorld(game.worldUHC.getName()) != null) {
                inv.setItem(pos, Hub.createItemStack(Material.GOLDEN_APPLE, "\u00A7fUHC", 0, "\u00A77Join " + game.worldUHC.getName()));
                pos++;
            }
        }
        inv.setItem(30, Hub.createItemStack(Material.ARROW, "\u00A7fBack", 0));
        inv.setItem(31, Hub.createItemStack(Material.BARRIER, "\u00A7cClose", 0));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inv)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.STAINED_GLASS_PANE) {
            return;
        }
        if (event.getRawSlot() == 30) {
            new me.solarlego.solarmain.gui.PlayGUI((Player) event.getWhoClicked());
        } else if (event.getRawSlot() == 31) {
            event.getWhoClicked().closeInventory();
        } else {
            String worldName;
            if (event.getRawSlot() == 10) {
                worldName = new UHCGame("miniUO" + UHC.getPlugin().getGames().size(), "miniUN" + UHC.getPlugin().getGames().size()).worldUHC.getName();
            } else {
                worldName = event.getCurrentItem().getItemMeta().getLore().get(0).split(" ")[1];
            }
            Party party = Party.getParty((Player) event.getWhoClicked());
            if (party == null || party.getLeader() != event.getWhoClicked()) {
                join((Player) event.getWhoClicked(), worldName);
            } else {
                for (Player player : party.getPlayers()) {
                    join(player, worldName);
                }
            }
            event.getWhoClicked().closeInventory();
        }

    }

    private void join(Player player, String worldName) {
        for (UHCGame game : UHC.getPlugin().getGames()) {
            if (Objects.equals(game.worldUHC.getName(), worldName) && !game.checkWorld(player.getWorld())) {
                player.sendMessage("\u00A77Sending you to " + game.worldUHC.getName() + "...");
                player.sendMessage("\n");
                if (game.time < -60) {
                    for (int i = 0; i < (20 - game.time) / 80; i++) {
                        int finalI = (20 - game.time) / 80 - i;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> player.sendMessage("\u00A77Generating World: " + finalI + " seconds."), i * 20L);
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getPlugin(), () -> player.teleport(game.worldUHC.getSpawnLocation().add(0.5, 0, 0.5)), 5 - game.time / 4L);
                } else if (Bukkit.getWorld(worldName) == null) {
                    player.sendMessage("\u00A7cThis game does not exist!");
                } else {
                    player.teleport(game.worldUHC.getSpawnLocation().add(0.5, 0, 0.5));
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inv)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == inv) {
            HandlerList.unregisterAll(this);
        }
    }

}
