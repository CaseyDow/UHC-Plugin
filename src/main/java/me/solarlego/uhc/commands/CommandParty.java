package me.solarlego.uhc.commands;

import me.solarlego.solarmain.Party;
import me.solarlego.uhc.UHC;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandParty implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        if (cmd.equals("/party warp") || cmd.equals("/p warp")) {
            Party party = Party.getParty(event.getPlayer());
            if (party == null) {
                return;
            }
            for (Player player : party.getPlayers()) {
                if (player == party.getLeader()) {
                    continue;
                }
                for (UHCGame game : UHC.getPlugin().getGames()) {
                    if (game.checkWorld(party.getLeader().getWorld()) && game.checkWorld(player.getWorld())) {
                        event.setMessage(event.getMessage() + " " + player.getName());
                    }
                }
            }
        }
    }

}
