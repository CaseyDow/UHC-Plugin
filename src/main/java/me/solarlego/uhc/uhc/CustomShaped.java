package me.solarlego.uhc.uhc;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CustomShaped extends ShapedRecipe {
    private final ItemStack output;
    private String[] rows;
    private Map<Character, ItemStack> ingredients = new HashMap<>();

    public CustomShaped(ItemStack result) {
        super(result);
        this.output = new ItemStack(result);
    }

    public CustomShaped shape(String... shape) {
        Validate.notNull(shape, "Must provide a shape");
        Validate.isTrue(shape.length > 0 && shape.length < 4, "Crafting recipes should be 1, 2, 3 rows, not ", shape.length);
        int var3 = shape.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String row = shape[var4];
            Validate.notNull(row, "Shape cannot have null rows");
            Validate.isTrue(row.length() > 0 && row.length() < 4, "Crafting rows should be 1, 2, or 3 characters, not ", row.length());
        }

        this.rows = new String[shape.length];

        System.arraycopy(shape, 0, this.rows, 0, shape.length);

        HashMap<Character, ItemStack> newIngredients = new HashMap<>();
        int var11 = shape.length;

        for(var3 = 0; var3 < var11; ++var3) {
            String row = shape[var3];
            char[] var7;
            int var8 = (var7 = row.toCharArray()).length;

            for(int var9 = 0; var9 < var8; ++var9) {
                Character c = var7[var9];
                newIngredients.put(c, this.ingredients.get(c));
            }
        }

        this.ingredients = newIngredients;
        return this;
    }

    public void setAnyIngredient(char key, Material ingredient, int raw) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        if (raw == -1) {
            raw = 32767;
        }

        this.ingredients.put(key, new ItemStack(ingredient, 1, (short)raw));
    }

    public Map<Character, ItemStack> getIngredientMap() {
        HashMap<Character, ItemStack> result = new HashMap<>();

        for (Map.Entry<Character, ItemStack> characterItemStackEntry : this.ingredients.entrySet()) {
            if (characterItemStackEntry.getValue() == null) {
                result.put(characterItemStackEntry.getKey(), null);
            } else {
                result.put(characterItemStackEntry.getKey(), characterItemStackEntry.getValue().clone());
            }
        }

        return result;
    }

    public String[] getShape() {
        return this.rows.clone();
    }

    public ItemStack getResult() {
        return this.output.clone();
    }
}
