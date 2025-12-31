package com.anahkenonje.mixedstacks;

// IMPORT THE RENDERER FROM THE CLIENT PACKAGE

import com.anahkenonje.mixedstacks.client.MixedStackRenderer;
import com.anahkenonje.mixedstacks.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class MixedStacksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Now MixedStackRenderer should be resolved
        System.out.println("Mixed Stacks Client Initialized!");
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.MIXED_STACK, new MixedStackRenderer());
    }
}