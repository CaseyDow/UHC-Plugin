package me.solarlego.uhc.gui;

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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class RecipesGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public RecipesGUI(UHCGame uhc, Player player) {
        inv = Bukkit.createInventory(null, 45, "Recipes");
        game = uhc;
        initializeItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems() {
        int i = 10;
        for (ItemStack stack : game.crafts.keySet()) {
            inv.setItem(i, stack);
            if (i % 9 == 7) {
                i += 3;
            } else {
                i++;
            }
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inv)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        Player p = (Player) event.getWhoClicked();
        new CraftGUI(game, p, event.getCurrentItem());
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
