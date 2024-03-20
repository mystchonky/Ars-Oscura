package com.mystchonky.arsoscura.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.setup.registry.RegistryHelper;
import com.mystchonky.arsoscura.ArsOscura;
import com.mystchonky.arsoscura.common.registrar.RecipeRegistrar;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hollingsworth.arsnouveau.setup.registry.RegistryHelper.getRegistryName;

public class ArcaneFusionRecipe extends EnchantingApparatusRecipe {
    public Enchantment baseEnchantment;
    public Enchantment resultEnchantment;

    public ArcaneFusionRecipe() {
        this.reagent = Ingredient.EMPTY;
        this.pedestalItems = new ArrayList<>();
        this.baseEnchantment = Enchantments.BINDING_CURSE;
        this.resultEnchantment = Enchantments.BINDING_CURSE;
        this.sourceCost = 0;
        this.id = new ResourceLocation(ArsOscura.MODID, "empty");
    }

    public ArcaneFusionRecipe(ResourceLocation id, Ingredient reagent, List<Ingredient> pedestalItems, Enchantment baseEnchantment, Enchantment resultEnchantment, int sourceCost) {
        this.id = id;
        this.reagent = reagent;
        this.pedestalItems = pedestalItems;
        this.baseEnchantment = baseEnchantment;
        this.resultEnchantment = resultEnchantment;
        this.sourceCost = sourceCost;
    }

    public ArcaneFusionRecipe copy() {
        return new ArcaneFusionRecipe(this.id, this.reagent, this.pedestalItems, this.baseEnchantment, this.resultEnchantment, this.sourceCost);
    }

    @Override
    public boolean excludeJei() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistrar.ENCHANTMENT_UPAGRADE.type().get();
    }

    @Override
    public boolean doesReagentMatch(ItemStack stack) {
        if (!reagent.test(stack))
            return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        int level = enchantments.getOrDefault(baseEnchantment, 0);
        return level > 0;
    }

    @Override
    public ItemStack getResult(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile) {
        return assemble(enchantingApparatusTile, null);
    }

    @Override
    public ItemStack assemble(EnchantingApparatusTile inv, RegistryAccess access) {
        ItemStack stack = inv.getStack().copy();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        int level = enchantments.getOrDefault(baseEnchantment, 0);
        enchantments.remove(baseEnchantment);
        enchantments.put(resultEnchantment, level);
        if (stack.getItem() == Items.ENCHANTED_BOOK)
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(enchantments, stack);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistrar.ENCHANTMENT_UPAGRADE.serializer().get();
    }

    @Override
    public JsonElement asRecipe() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", RecipeRegistrar.ENCHANTMENT_UPAGRADE.type().getId().toString());
        jsonobject.addProperty("baseEnchantment", RegistryHelper.getRegistryName(baseEnchantment).toString());
        jsonobject.addProperty("resultEnchantment", RegistryHelper.getRegistryName(resultEnchantment).toString());
        jsonobject.addProperty("sourceCost", getSourceCost());

        JsonArray pedestalArr = new JsonArray();
        for (Ingredient i : this.pedestalItems) {
            JsonObject object = new JsonObject();
            object.add("item", i.toJson());
            pedestalArr.add(object);
        }
        jsonobject.add("pedestalItems", pedestalArr);

        JsonArray reagent = new JsonArray();
        reagent.add(this.reagent.toJson());
        jsonobject.add("reagent", reagent);

        return jsonobject;
    }

    public static class Serializer implements RecipeSerializer<ArcaneFusionRecipe> {

        @Override
        public ArcaneFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Enchantment baseEnchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "baseEnchantment")));
            Enchantment resultEnchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "resultEnchantment")));
            int manaCost = GsonHelper.getAsInt(json, "sourceCost", 0);
            JsonArray pedestalItems = GsonHelper.getAsJsonArray(json, "pedestalItems");
            Ingredient reagent = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "reagent"));

            List<Ingredient> stacks = new ArrayList<>();

            for (JsonElement e : pedestalItems) {
                JsonObject obj = e.getAsJsonObject();
                Ingredient input;
                if (GsonHelper.isArrayNode(obj, "item")) {
                    input = Ingredient.fromJson(GsonHelper.getAsJsonArray(obj, "item"));
                } else {
                    input = Ingredient.fromJson(GsonHelper.getAsJsonObject(obj, "item"));
                }
                stacks.add(input);
            }
            return new ArcaneFusionRecipe(recipeId, reagent, stacks, baseEnchantment, resultEnchantment, manaCost);
        }

        @Override
        public @Nullable ArcaneFusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int length = buffer.readInt();
            Ingredient reagent = Ingredient.fromNetwork(buffer);
            String baseEnchantID = buffer.readUtf();
            String resultEnchantID = buffer.readUtf();
            int manaCost = buffer.readInt();
            List<Ingredient> stacks = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                try {
                    stacks.add(Ingredient.fromNetwork(buffer));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            return new ArcaneFusionRecipe(recipeId, reagent, stacks, ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(baseEnchantID)), ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(resultEnchantID)), manaCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ArcaneFusionRecipe recipe) {
            buf.writeInt(recipe.pedestalItems.size());
            recipe.reagent.toNetwork(buf);
            buf.writeUtf(getRegistryName(recipe.baseEnchantment).toString());
            buf.writeUtf(getRegistryName(recipe.resultEnchantment).toString());
            buf.writeInt(recipe.getSourceCost());
            for (Ingredient i : recipe.pedestalItems) {
                i.toNetwork(buf);
            }
        }
    }
}