package me.solarlego.uhc.gui.kits;

import me.solarlego.uhc.UHC;
import me.solarlego.uhc.uhc.EnchantPair;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class LooterGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public LooterGUI(UHCGame uhc, Player player) {
        inv = Bukkit.createInventory(null, 36, "Looter");
        game = uhc;
        initializeItems();
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems() {
        inv.setItem(11, game.createItemStack(Material.IRON_SWORD, "", 1, 0, new EnchantPair(Enchantment.LOOT_BONUS_MOBS, 1)));
        inv.setItem(13, new ItemStack(Material.SULPHUR, 3));
        inv.setItem(15, new ItemStack(Material.FEATHER, 4));
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
