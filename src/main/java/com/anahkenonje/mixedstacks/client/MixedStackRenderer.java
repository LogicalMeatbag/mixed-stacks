package com.anahkenonje.mixedstacks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

public class MixedStackRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {

        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);

        // Safety Check: If empty, draw a Barrier so you know something is wrong
        // (In normal gameplay, your inventoryTick logic should prevent empty bags from existing)
        if (contents == null || contents.isEmpty()) {
            renderItem(new ItemStack(Items.BARRIER), mode, matrices, vertexConsumers, light, overlay);
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int count = 0;

        // Iterate through the items inside
        for (ItemStack innerItem : contents.items()) {
            if (count >= 2) break; // We only render the first 2 items

            matrices.pushPose();

            if (count == 0) {
                // --- SLOT 1: Top-Left (Background) ---
                // Translate: Center (0.5), then move Left (-0.2) and Up (+0.2)
                // Z-Index: 0.51 ensures it's in front of the slot background
                matrices.translate(0.5f - 0.12f, 0.5f + 0.12f, 0.51f);

                // Scale: 70% size
                matrices.scale(0.75f, 0.75f, 0.75f);
            } else {
                // --- SLOT 2: Bottom-Right (Foreground) ---
                // Translate: Center (0.5), then move Right (+0.2) and Down (-0.2)
                // Z-Index: 0.52 ensures it sits ON TOP of the first item
                matrices.translate(0.5f + 0.12f, 0.5f - 0.12f, 0.52f);

                // Scale: 70% size
                matrices.scale(0.75f, 0.75f, 0.75f);
            }

            // Render the inner item
            BakedModel model = itemRenderer.getModel(innerItem, null, null, 0);
            itemRenderer.render(innerItem, mode, false, matrices, vertexConsumers, light, overlay, model);

            matrices.popPose();
            count++;
        }
    }

    // Helper to draw a single item (used for the Barrier fallback)
    private void renderItem(ItemStack itemToDraw, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(itemToDraw, null, null, 0);

        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        itemRenderer.render(itemToDraw, mode, false, matrices, vertexConsumers, light, overlay, model);
        matrices.popPose();
    }
}