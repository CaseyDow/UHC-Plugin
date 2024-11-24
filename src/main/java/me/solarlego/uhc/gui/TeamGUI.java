package me.solarlego.uhc.gui;

import me.solarlego.solarmain.Stats;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.uhc.UHC;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeamGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public TeamGUI(UHCGame uhc, Player player) {
        inv = Bukkit.createInventory(null, 27, "Teams");
        game = uhc;
        initializeItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems() {
        inv.setItem(10, game.createItemStack(Material.WOOL, "\u00A7cRed Team", 1, 14));
        inv.setItem(11, game.createItemStack(Material.WOOL, "\u00A76Gold Team", 1, 4));
        inv.setItem(12, game.createItemStack(Material.WOOL, "\u00A7aGreen Team", 1, 5));
        inv.setItem(13, game.createItemStack(Material.WOOL, "\u00A7bAqua Team", 1, 3));
        inv.setItem(14, game.createItemStack(Material.WOOL, "\u00A79Blue Team", 1, 11));
        inv.setItem(15, game.createItemStack(Material.WOOL, "\u00A75Purple Team", 1, 10));
        inv.setItem(16, game.createItemStack(Material.WOOL, "\u00A78Black Team", 1, 15));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inv)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        Player p = (Player) event.getWhoClicked();

        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        game.players.get(p.getUniqueId()).setTeam(itemName.substring(2, itemName.indexOf(" ")));
        p.sendMessage("\u00A7eYou joined " + itemName + "\u00A7e!");
        p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
        p.getInventory().setItem(2, Hub.createItemStack(Material.WOOL, "\u00A7fTeam Selector", (int) clickedItem.getDurability(), "\u00A7eRight Click to Open!"));

        p.setPlayerListName(Stats.get(p.getUniqueId()).getPrefix() + itemName.substring(0, 2) + p.getName());
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
