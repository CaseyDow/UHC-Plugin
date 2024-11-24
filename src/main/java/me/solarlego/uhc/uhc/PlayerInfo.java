package me.solarlego.uhc.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerInfo {

    private final UUID player;
    private String team = "Red";
    private String teamColor = "c";
    private String kit = "Stone Tools";
    private int kills = 0;
    private int coins = 0;
    private boolean isDead = false;
    private Dummy dummy;

    public PlayerInfo(Player p) {
        player = p.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
        switch (team) {
            case "Red":
                teamColor = "c";
                break;
            case "Gold":
                teamColor = "6";
                break;
            case "Green":
                teamColor = "a";
                break;
            case "Aqua":
                teamColor = "b";
                break;
            case "Blue":
                teamColor = "9";
                break;
            case "Purple":
                teamColor = "5";
                break;
            case "Black":
                teamColor = "0";
                break;
            default:
                teamColor = "7";
                break;
        }
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public int getKills() {
        return kills;
    }

    public void addKills(int kills) {
        this.kills += kills;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isDead() {
        return isDead;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public Dummy getDummy() {
        return dummy;
    }

    public void makeDummy(Player player, Location location, int time) {
        if (this.dummy == null || !this.dummy.exists()) {
            this.dummy = new Dummy(player, location, time);
        }
    }

}
