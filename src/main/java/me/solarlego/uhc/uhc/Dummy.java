package me.solarlego.uhc.uhc;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class Dummy {

    private final int disconnectTime;
    private final ItemStack[] inv;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> pots;
    private final Location loc;
    private final Zombie dummy;
    private final double health;
    private final double absorption;
    private final int exp;
    private boolean exist = true;

    public Dummy(Player player, Location loc, int time) {
        this.loc = loc;
        this.inv = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.pots = player.getActivePotionEffects();
        this.disconnectTime = time;
        this.health = player.getHealth();
        this.absorption = ((CraftPlayer) player).getHandle().getAbsorptionHearts();
        this.exp = Math.min(player.getLevel() * 7, 100);

        dummy = loc.getWorld().spawn(loc, Zombie.class);

        dummy.setCustomNameVisible(true);
        dummy.setCustomName(player.getName() + " \u00A7f(" + (int) Math.round(absorption + health) + ")");
        dummy.setMaxHealth(60);
        dummy.setHealth(absorption + health);
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) dummy).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        nmsEntity.c(tag);
        tag.setInt("NoAI", 1);
        nmsEntity.f(tag);

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(player.getName());
        skull.setOwner(player.getName());
        item.setItemMeta(skull);

        dummy.getEquipment().setHelmet(item);
    }

    public UUID getUUID() {
        return dummy.getUniqueId();
    }

    public void replace(Player player) {
        player.setBedSpawnLocation(dummy.getLocation(), true);
        player.teleport(dummy.getLocation());
        dummy.remove();
        exist = false;
    }

    public void rejoin(Player player, int time) {
        ArrayList<PotionEffect> effects = new ArrayList<>();
        for (PotionEffect effect : pots) {
            effects.add(new PotionEffect(effect.getType(), effect.getDuration() + disconnectTime - time, effect.getAmplifier()));
        }

        player.getInventory().setContents(inv);
        player.getInventory().setArmorContents(armor);
        player.addPotionEffects(effects);
        player.setHealth(Math.min(dummy.getHealth(), health));
        ((CraftPlayer) player).getHandle().setAbsorptionHearts((float) Math.min(absorption, dummy.getHealth() - health));
        player.teleport(loc);

        dummy.remove();
        exist = false;
    }

    public boolean exists() {
        return exist;
    }

    public int getExp() {
        return exp;
    }

    public List<ItemStack> getItems() {
        return Arrays.asList((ItemStack[]) ArrayUtils.addAll(inv, armor));
    }
}
