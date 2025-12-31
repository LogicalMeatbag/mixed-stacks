package com.anahkenonje.mixedstacks.mixin;

import com.anahkenonje.mixedstacks.item.MixedStackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // This targets the "new ItemParticleOption(...)" call inside spawnItemParticles.
    // We modify the 2nd argument (index 1), which is the ItemStack.
    @ModifyArg(
            method = "spawnItemParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/particles/ItemParticleOption;<init>(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 1
    )
    private ItemStack mixedStacks$changeParticleStack(ItemStack stack) {
        // 1. Check if the item making particles is our Mixed Stack
        if (stack.getItem() instanceof MixedStackItem) {
            BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);

            // 2. If it has items inside, use the first item (the food) for the particle!
            if (contents != null && !contents.isEmpty()) {
                return contents.items().iterator().next();
            }
        }

        // 3. Otherwise, behave normally
        return stack;
    }
}