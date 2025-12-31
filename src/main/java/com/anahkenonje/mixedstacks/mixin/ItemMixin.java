package com.anahkenonje.mixedstacks.mixin;

import com.anahkenonje.mixedstacks.config.MixedStacksConfig;
import com.anahkenonje.mixedstacks.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// Do NOT change this (besides adding debug messages). It WILL break... trust me.
@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    public void mixedStacks$combineItems(ItemStack cursorStack, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        // 1. Only handle Right-Click (Secondary)
        if (action != ClickAction.SECONDARY) return;

        // 2. Validate Items
        ItemStack slotStack = slot.getItem();
        if (cursorStack.isEmpty() || slotStack.isEmpty()) return;

        // 3. Prevent nesting bundles inside themselves
        if (cursorStack.is(ModItems.MIXED_STACK) || slotStack.is(ModItems.MIXED_STACK)) return;

        // 4. Check Config
        if (MixedStacksConfig.INSTANCE == null) return;

        if (MixedStacksConfig.INSTANCE.canMix(slotStack, cursorStack)) {

            // 5. Try to combine them
            ItemStack bundle = new ItemStack(ModItems.MIXED_STACK);
            BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);

            // Add Slot Item First (copy it so we don't modify the original slot yet)
            mutable.tryInsert(slotStack.copy());

            // Add Cursor Item Second (THIS MODIFIES cursorStack!)
            int insertedCount = mutable.tryInsert(cursorStack);

            // 6. If we successfully moved ANY items into the bundle...
            if (insertedCount > 0) {
                // Apply the new Bundle to the slot
                bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                slot.set(bundle);

                // Cursor stack is AUTOMATICALLY reduced by tryInsert.
                // We don't need to setCount() manually.

                // Play Sound
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);

                // Tell game we handled it (Stops vanilla swap/pickup)
                cir.setReturnValue(true);
            }
        }
    }
}