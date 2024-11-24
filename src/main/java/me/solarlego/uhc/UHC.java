package me.solarlego.uhc;

import me.solarlego.uhc.commands.CommandPlay;
import me.solarlego.uhc.uhc.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;

public class UHC extends JavaPlugin {

    private static UHC instance;
    private ArrayList<UHCGame> games;

    @Override
    public void onEnable() {
        instance = this;
        games = new ArrayList<>();
        Bukkit.getServer().getPluginManager().registerEvents(new CommandPlay(), this);

        saveDefaultConfig();
        File file = new File(getDataFolder(), "players.yml");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        for (UHCGame game : games) {
            game.shutdown();
        }
    }
    
    public static UHC getPlugin() {
        return instance;
    }

    public void updatePlayerFile(String path, int val) {
        FileConfiguration playersFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "players.yml"));

        playersFile.set(path, val + playersFile.getInt(path));
        playersFile.options().copyDefaults(true);
        try {
            playersFile.save(new File(getDataFolder(), "players.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UHCGame> getGames() {
        return games;
    }

    public void addGame(UHCGame game) {
        this.games.add(game);
    }

}
