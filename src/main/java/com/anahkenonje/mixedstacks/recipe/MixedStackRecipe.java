package com.anahkenonje.mixedstacks.recipe;

import com.anahkenonje.mixedstacks.config.MixedStacksConfig;
import com.anahkenonje.mixedstacks.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class MixedStackRecipe extends CustomRecipe {

    public MixedStackRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }

        if (stacks.size() != 2) return false;

        ItemStack itemA = stacks.get(0);
        ItemStack itemB = stacks.get(1);

        if (itemA.is(ModItems.MIXED_STACK) || itemB.is(ModItems.MIXED_STACK)) return false;

        if (!MixedStacksConfig.INSTANCE.canMix(itemA, itemB)) return false;

        // Ensure that 1 of A and 1 of B can fit together
        int weightA = (64 / itemA.getMaxStackSize()); // Count is assumed 1 for matching
        int weightB = (64 / itemB.getMaxStackSize());

        return (weightA + weightB) <= 64;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();

                // CRITICAL FIX: Only put 1 of the item into the result,
                // because the crafting grid only consumes 1.
                copy.setCount(1);

                stacks.add(copy);
            }
        }

        if (stacks.size() != 2) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(ModItems.MIXED_STACK);
        result.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(stacks));

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return com.anahkenonje.mixedstacks.MixedStacks.MIXED_STACK_RECIPE_SERIALIZER;
    }
}