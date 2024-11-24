package me.solarlego.uhc.uhc;

import org.bukkit.enchantments.Enchantment;

public class EnchantPair {

    private final Enchantment enchantment;
    private final Integer level;

    public EnchantPair(Enchantment ench, Integer lvl) {
        enchantment = ench;
        level = lvl;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public Integer getLevel() {
        return level;
    }
}
