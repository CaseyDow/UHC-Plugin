package me.solarlego.uhc.gui.kits;

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


public class KitsGUI implements Listener {

    private final Inventory inv;
    private final UHCGame game;

    public KitsGUI(UHCGame uhc, Player player) {
        inv = Bukkit.createInventory(null, 36, "Kits");
        game = uhc;
        initializeItems(player);
        Bukkit.getServer().getPluginManager().registerEvents(this, UHC.getPlugin());
        player.openInventory(inv);
    }

    public void initializeItems(Player player) {
        inv.setItem(11, Hub.createItemStack(Material.STONE_PICKAXE, "\u00A7fStone Tools", 0, "\u00A7eClick to View!"));
        inv.setItem(13, Hub.createItemStack(Material.IRON_SWORD, "\u00A7fLooter", 0, "\u00A7eClick to View!"));
        inv.setItem(15, Hub.createItemStack(Material.BOW, "\u00A7fArcher", 0, "\u00A7eClick to View!"));
        String[] names = new String[] {"Stone Tools", "Looter", "Archer"};
        for (int i = 0; i < 3; i ++) {
            int dmg = 8;
            if (game.players.get(player.getUniqueId()).getKit().equals(names[i])) {
                dmg = 10;
            }
            inv.setItem(i * 2 + 20, game.createItemStack(Material.INK_SACK, "\u00A7fSelect", 1, dmg));
        }
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
        if (event.getRawSlot() == 11) {
            new StoneToolsGUI(game, p);
        } else if (event.getRawSlot() == 13) {
            new LooterGUI(game, p);
        } else if (event.getRawSlot() == 15) {
            new ArcherGUI(game, p);
        } else {
            String kit = null;
            if (event.getRawSlot() == 20) {
                kit = "Stone Tools";
                p.getInventory().setItem(4, Hub.createItemStack(Material.STONE_PICKAXE, "\u00A7fTeam Selector", 0, "\u00A7eRight Click to Open!"));
            } else if (event.getRawSlot() == 22) {
                kit = "Looter";
                p.getInventory().setItem(4, Hub.createItemStack(Material.IRON_SWORD, "\u00A7fTeam Selector", 0, "\u00A7eRight Click to Open!"));
            } else if (event.getRawSlot() == 24) {
                kit = "Archer";
                p.getInventory().setItem(4, Hub.createItemStack(Material.BOW, "\u00A7fTeam Selector", 0, "\u00A7eRight Click to Open!"));
            }
            if (kit != null) {
                game.players.get(p.getUniqueId()).setKit(kit);
            }
            p.sendMessage("\u00A7eYou selected the " + kit + " kit!");
            p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
            initializeItems(p);
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
