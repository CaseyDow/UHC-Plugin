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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CraftGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public CraftGUI(UHCGame uhc, Player player, ItemStack item) {
        inv = Bukkit.createInventory(null, 54, "Recipes");
        game = uhc;
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        displayCraft(game.crafts.get(item), item);
        player.openInventory(inv);
    }

    public void displayCraft(Material[] slots, ItemStack result) {
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, game.createItemStack(Material.STAINED_GLASS_PANE, " ", 1, 15));
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = Material.AIR;
            }
        }
        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int dataVal = 0;
                if (slots[i] == Material.SKULL_ITEM) {
                    dataVal = 3;
                }
                inv.setItem(10 + x + y * 9, game.createItemStack(slots[i], "", 1, dataVal));
                i++;
            }
        }
        inv.setItem(24, result);
        inv.setItem(48, game.createItemStack(Material.ARROW, "\u00A7fBack", 1, 0));
        inv.setItem(49, game.createItemStack(Material.BARRIER, "\u00A7cClose", 1, 0));

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inv)) {
            return;
        }
        if (event.getRawSlot() == 48) {
            new RecipesGUI(game, (Player) event.getWhoClicked());
        } else if (event.getRawSlot() == 49) {
            event.getWhoClicked().closeInventory();
        }
        event.setCancelled(true);
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
