package me.solarlego.uhc.uhc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;

public class CustomCrafts {

    private final HashMap<ItemStack, Material[]> crafts;
    private final UHCGame game;

    public CustomCrafts(UHCGame uhc) {
        crafts = new LinkedHashMap<>();
        game = uhc;
        initializeCrafts();
    }

    public void addShaped(String[] shape, ItemStack result, MaterialValuePair... pairs) {
        if (shape.length == 3) {
            CustomShaped recipe = new CustomShaped(result);
            recipe.shape(shape[0], shape[1], shape[2]);
            Material[] mats = new Material[9];
            String shapeStr = shape[0].concat(shape[1]).concat(shape[2]);
            for (MaterialValuePair pair : pairs) {
                recipe.setAnyIngredient(pair.getValue(), pair.getMaterial(), 32767);
                while (shapeStr.contains(pair.getValue().toString())) {
                    mats[shapeStr.indexOf(pair.getValue())] = pair.getMaterial();
                    int index = shapeStr.indexOf(pair.getValue());
                    shapeStr = shapeStr.substring(0, index) + " " + shapeStr.substring(index + 1);
                }
            }
            Bukkit.addRecipe(recipe);
            crafts.put(result, mats);
        }
    }

    public void addShapeless(ItemStack result, MaterialAmountPair... pairs) {
        ShapelessRecipe recipe = new ShapelessRecipe(result);
        Material[] mats = new Material[9];
        int t = 0;
        for (MaterialAmountPair pair : pairs) {
            recipe.addIngredient(pair.getAmount(), pair.getMaterial());
            for (int i = 0; i < pair.getAmount(); i++) {
                mats[t] = pair.getMaterial();
                t++;
            }
        }
        crafts.put(result, mats);
        Bukkit.addRecipe(recipe);
    }

    public void initializeCrafts() {
        addShaped(new String[] {"ICI", "ISI", " S "}, game.createItemStack(Material.IRON_PICKAXE, "\u00A7fQuickaxe", 1, 0, new EnchantPair(Enchantment.DIG_SPEED, 1)), new MaterialValuePair(Material.IRON_ORE, "I"), new MaterialValuePair(Material.COAL_BLOCK, "C"), new MaterialValuePair(Material.STICK, "S"));
        addShaped(new String[] {"III", "ICI", "III"}, new ItemStack(Material.IRON_INGOT, 8), new MaterialValuePair(Material.IRON_ORE, "I"), new MaterialValuePair(Material.COAL, "C"));
        addShaped(new String[] {"GGG", "GCG", "GGG"}, new ItemStack(Material.GOLD_INGOT, 8), new MaterialValuePair(Material.GOLD_ORE, "G"), new MaterialValuePair(Material.COAL, "C"));
        addShaped(new String[] {"III", "SHS", "   "}, game.createItemStack(Material.IRON_HELMET, "\u00A7fReinforced Helmet", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.STRING, "S"), new MaterialValuePair(Material.IRON_HELMET, "H"));
        addShaped(new String[] {"F F", "ICI", "III"}, game.createItemStack(Material.IRON_CHESTPLATE, "\u00A7fReinforced Chestplate", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1), new EnchantPair(Enchantment.PROTECTION_PROJECTILE, 2)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.FLINT, "F"), new MaterialValuePair(Material.IRON_CHESTPLATE, "C"));
        addShaped(new String[] {"III", "ILI", "F F"}, game.createItemStack(Material.IRON_LEGGINGS, "\u00A7fReinforced Leggings", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1), new EnchantPair(Enchantment.PROTECTION_FIRE, 2)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.FLINT, "F"), new MaterialValuePair(Material.IRON_LEGGINGS, "L"));
        addShaped(new String[] {"I I", "SBS", "   "}, game.createItemStack(Material.IRON_BOOTS, "\u00A7fReinforced Boots", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.STRING, "S"), new MaterialValuePair(Material.IRON_BOOTS, "B"));
        addShaped(new String[] {"IRI", "ISI", "IRI"}, game.createItemStack(Material.IRON_SWORD, "\u00A7fReinforced Sword", 1, 0, new EnchantPair(Enchantment.DAMAGE_ALL, 1)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.REDSTONE_BLOCK, "R"), new MaterialValuePair(Material.IRON_SWORD, "S"));
        addShaped(new String[] {"BIB", "ISI", "BIB"}, game.createItemStack(Material.IRON_SWORD, "\u00A7fSword of \u00A7cFire", 1, 0, new EnchantPair(Enchantment.DAMAGE_ALL, 1), new EnchantPair(Enchantment.FIRE_ASPECT, 2)), new MaterialValuePair(Material.BLAZE_POWDER, "B"), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.IRON_SWORD, "S"));
        addShaped(new String[] {"IDI", "ISI", "ILI"}, game.createItemStack(Material.DIAMOND_SWORD, "\u00A7dInfused \u00A7fSword", 1, 0, new EnchantPair(Enchantment.DAMAGE_ALL, 1)), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.DIAMOND, "D"), new MaterialValuePair(Material.DIAMOND_SWORD, "S"), new MaterialValuePair(Material.LAPIS_BLOCK, "L"));

        ItemStack helmetStability = game.createItemStack(Material.DIAMOND_HELMET, "\u00A7fHelmet of \u00A7aStability", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2), new EnchantPair(Enchantment.PROTECTION_PROJECTILE, 3));
        helmetStability = addAttribute(helmetStability, "generic.knockbackResistance", 1.0, 0);
        addShaped(new String[] {"RER", "IHI", "   "}, helmetStability, new MaterialValuePair(Material.REDSTONE_BLOCK, "R"), new MaterialValuePair(Material.EMERALD, "E"), new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.DIAMOND_HELMET, "H"));
        ItemStack chestplateLove = game.createItemStack(Material.DIAMOND_CHESTPLATE, "\u00A7fChestplate of \u00A7cLove", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2), new EnchantPair(Enchantment.PROTECTION_FIRE, 2));
        chestplateLove = addAttribute(chestplateLove, "generic.maxHealth", 0.5, 1);
        addShaped(new String[] {"IGI", "ICI", "   "}, chestplateLove, new MaterialValuePair(Material.IRON_INGOT, "I"), new MaterialValuePair(Material.GOLDEN_APPLE, "G"), new MaterialValuePair(Material.DIAMOND_CHESTPLATE, "C"));
        ItemStack leggingsPower = game.createItemStack(Material.DIAMOND_LEGGINGS, "\u00A7fLeggings of \u00A74Power", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2), new EnchantPair(Enchantment.PROTECTION_FIRE, 3));
        leggingsPower = addAttribute(leggingsPower, "generic.attackDamage", 0.2, 1);
        addShaped(new String[] {"OPO", "BLB", "   "}, leggingsPower, new MaterialValuePair(Material.OBSIDIAN, "O"), new MaterialValuePair(Material.ENDER_PEARL, "P"), new MaterialValuePair(Material.BLAZE_POWDER, "B"), new MaterialValuePair(Material.DIAMOND_LEGGINGS, "L"));
        ItemStack shoesSpeed = game.createItemStack(Material.DIAMOND_BOOTS, "\u00A7fShoes of \u00A7bSpeed", 1, 0, new EnchantPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1), new EnchantPair(Enchantment.DEPTH_STRIDER, 2));
        shoesSpeed = addAttribute(shoesSpeed, "generic.movementSpeed", .02, 0);
        addShaped(new String[] {" P ", " B ", "FPF"}, shoesSpeed, new MaterialValuePair(Material.BLAZE_POWDER, "P"), new MaterialValuePair(Material.DIAMOND_BOOTS, "B"), new MaterialValuePair(Material.FEATHER, "F"));
        ItemStack bowWind = game.createItemStack(Material.BOW, "\u00A7fBow of the \u00A7bWind", 1, 0, new EnchantPair(Enchantment.ARROW_DAMAGE, 1), new EnchantPair(Enchantment.ARROW_KNOCKBACK, 1));
        bowWind = addAttribute(bowWind, "generic.movementSpeed", 0.02, 0);
        addShaped(new String[] {"FSF", "FBF", "FSF"}, bowWind, new MaterialValuePair(Material.FEATHER, "F"), new MaterialValuePair(Material.SUGAR, "S"), new MaterialValuePair(Material.BOW, "B"));

        addShaped(new String[] {"FCF", "FBF", "FPF"}, game.createItemStack(Material.BOW, "\u00A7cScorched \u00A7fBow", 1, 0, new EnchantPair(Enchantment.ARROW_DAMAGE, 2), new EnchantPair(Enchantment.ARROW_FIRE, 1)), new MaterialValuePair(Material.FLINT, "F"), new MaterialValuePair(Material.FIREBALL, "C"), new MaterialValuePair(Material.BOW, "B"), new MaterialValuePair(Material.BLAZE_POWDER, "P"));
        addShaped(new String[] {"GLG", "GSG", "GLG"}, new ItemStack(Material.BLAZE_ROD), new MaterialValuePair(Material.GLOWSTONE_DUST, "G"), new MaterialValuePair(Material.LAVA_BUCKET, "L"), new MaterialValuePair(Material.SULPHUR, "S"));
        addShapeless(new ItemStack(Material.OBSIDIAN), new MaterialAmountPair(Material.LAVA_BUCKET, 1), new MaterialAmountPair(Material.WATER_BUCKET, 1));

        ItemStack goldenHead = game.createItemStack(Material.SKULL_ITEM, "\u00A76Golden Head", 1, 3);
        goldenHead = setLore(goldenHead, Arrays.asList("\u00A7cRegeneration III \u00A7ffor 7 seconds", "\u00A7bSpeed I \u00A7ffor 20 seconds", "\u00A7eAbsorption II \u00A7ffor 2 minutes"));
        goldenHead = customHead(goldenHead, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjMzNWMzOWJkNmI1NDkyOTQyY2VlZDZjYjljODkwZjJiMzg2ZjZlNDJjYzQzMGUzODdlNTcxZWUzZDY3NWNiZSJ9fX0=");
        addShaped(new String[] {"GGG", "GHG", "GGG"}, goldenHead, new MaterialValuePair(Material.GOLD_INGOT, "G"), new MaterialValuePair(Material.SKULL_ITEM, "H"));
        ItemStack strongHead = game.createItemStack(Material.SKULL_ITEM, "\u00A74Strong Head", 1, 3);
        strongHead = setLore(strongHead, Arrays.asList("\u00A74Strength I \u00A7ffor 1 minute", "\u00A77Resistance I \u00A7ffor 1 minute"));
        strongHead = customHead(strongHead, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ZmMGJlZTU5MWU1ZjAwMDBlZjE2Zjk2NmI5NDlhZGNiNWMyZjQwOWExNGNjZmM1YjkxMjIyZmQ5MjUwNDVkYiJ9fX0=");
        addShaped(new String[] {"RGR", "RHR", "RRR"}, strongHead, new MaterialValuePair(Material.REDSTONE, "R"), new MaterialValuePair(Material.SULPHUR, "G"), new MaterialValuePair(Material.SKULL_ITEM, "H"));
        ItemStack speedHead = game.createItemStack(Material.SKULL_ITEM, "\u00A7bSpeed Head", 1, 3);
        speedHead = setLore(speedHead, Arrays.asList("\u00A7cRegeneration III \u00A7ffor 4 seconds", "\u00A7bSpeed II \u00A7ffor 1 minute", "\u00A7eAbsorption I \u00A7ffor 2 minutes"));
        speedHead = customHead(speedHead, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkZjg2Y2M0NjlhYjQ5MTU1ZTY3NWJiMzA0NDRjYjI2N2RjNDNkZmIzNTRkMTVkMWMwYzY2NWJlNTg1MjcifX19");
        addShaped(new String[] {"SSS", "SHS", "SSS"}, speedHead, new MaterialValuePair(Material.SUGAR, "S"), new MaterialValuePair(Material.SKULL_ITEM, "H"));

    }

    public HashMap<ItemStack, Material[]> getCrafts() {
        return crafts;
    }

    public ItemStack addAttribute(ItemStack item, String type, Double amt, int mode) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
            compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        NBTTagList modifiers = new NBTTagList();

        NBTTagCompound mod = new NBTTagCompound();
        mod.set("AttributeName", new NBTTagString(type));
        mod.set("Name", new NBTTagString(type));
        mod.set("Amount", new NBTTagDouble(amt));
        mod.set("Operation", new NBTTagInt(mode));
        mod.set("UUIDLeast", new NBTTagInt(894654));
        mod.set("UUIDMost", new NBTTagInt(2872));
        modifiers.add(mod);

        compound.set("AttributeModifiers", modifiers);
        nmsStack.setTag(compound);
        item = CraftItemStack.asBukkitCopy(nmsStack);
        return item;
    }

    public ItemStack customHead(ItemStack item, String url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        ItemMeta meta = item.getItemMeta();
        try {
            Field mtd = meta.getClass().getDeclaredField("profile");
            mtd.setAccessible(true);
            mtd.set(meta, profile);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack setLore(ItemStack item, List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

}

class MaterialValuePair {

    private final Material material;
    private final Character value;

    public MaterialValuePair(Material mat, String val) {
        material = mat;
        value = val.charAt(0);
    }

    public Material getMaterial() {
        return material;
    }

    public Character getValue() {
        return value;
    }
}

class MaterialAmountPair {

    private final Material material;
    private final Integer amount;

    public MaterialAmountPair(Material mat, Integer amt) {
        material = mat;
        amount = amt;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getAmount() {
        return amount;
    }
}