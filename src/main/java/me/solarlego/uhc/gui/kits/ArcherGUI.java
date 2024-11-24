package me.solarlego.uhc.gui.kits;

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


public class ArcherGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public ArcherGUI(UHCGame uhc, Player player) {
        inv = Bukkit.createInventory(null, 36, "Archer");
        game = uhc;
        initializeItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems() {
        inv.setItem(12, new ItemStack(Material.STRING, 8));
        inv.setItem(14, new ItemStack(Material.ARROW, 32));
        inv.setItem(30, game.createItemStack(Material.ARROW, "\u00A7fBack", 1, 0));
        inv.setItem(31, game.createItemStack(Material.BARRIER, "\u00A7cClose", 1, 0));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inv)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 30) {
                event.getWhoClicked().closeInventory();
                new KitsGUI(game, (Player) event.getWhoClicked());
            } else if (event.getRawSlot() == 31) {
                event.getWhoClicked().closeInventory();
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
