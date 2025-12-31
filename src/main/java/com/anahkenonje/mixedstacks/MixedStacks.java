package com.anahkenonje.mixedstacks;

import com.anahkenonje.mixedstacks.config.MixedStacksConfig;
import com.anahkenonje.mixedstacks.item.ModItems;
import com.anahkenonje.mixedstacks.recipe.MixedStackRecipe;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixedStacks implements ModInitializer {
    public static final String MOD_ID = "mixed-stacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Define the Serializer
    public static final RecipeSerializer<MixedStackRecipe> MIXED_STACK_RECIPE_SERIALIZER =
            new SimpleCraftingRecipeSerializer<>(MixedStackRecipe::new);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Mixed Stacks...");

        // 1. LOAD CONFIG (Fixes the NullPointerException)
        MixedStacksConfig.load();

        // 2. REGISTER ITEMS (Fixes missing items)
        ModItems.registerModItems();

        // 3. REGISTER RECIPE SERIALIZER
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "crafting_mixed_stack"),
                MIXED_STACK_RECIPE_SERIALIZER);
    }
}