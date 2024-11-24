package me.solarlego.uhc.commands;

import me.solarlego.uhc.gui.RecipesGUI;
import me.solarlego.uhc.gui.TeamGUI;
import me.solarlego.uhc.gui.kits.KitsGUI;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandGame implements Listener {

    private final UHCGame game;

    public CommandGame(UHCGame uhc) {
        game = uhc;
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            String cmd = event.getMessage().toLowerCase();
            switch (cmd) {
                case "/recipe":
                case "/recipes":
                case "/craft":
                case "/crafts":
                    new RecipesGUI(game, event.getPlayer());
                    event.setCancelled(true);
                    break;
                case "/team":
                case "/teams":
                    if (game.time < 0) {
                        new TeamGUI(game, event.getPlayer());
                    } else {
                        event.getPlayer().sendMessage("\u00A7cYou can not use this anymore!");
                    }
                    event.setCancelled(true);
                    break;
                case "/kit":
                case "/kits":
                    if (game.time < 0) {
                        new KitsGUI(game, event.getPlayer());
                    } else {
                        event.getPlayer().sendMessage("\u00A7cYou can not use this anymore!");
                    }
                    event.setCancelled(true);
                    break;
                case "/end":
                    if (event.getPlayer().hasPermission("solarlego.command.end")) {
                        game.shutdown();
                        event.setCancelled(true);
                    }
                    break;
            }
        }
    }

}
