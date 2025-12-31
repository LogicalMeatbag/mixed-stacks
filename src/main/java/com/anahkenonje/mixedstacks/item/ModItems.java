package com.anahkenonje.mixedstacks.item;

import com.anahkenonje.mixedstacks.MixedStacks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item MIXED_STACK = registerItem("mixed_stack", new MixedStackItem(new Item.Properties().stacksTo(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MixedStacks.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MixedStacks.LOGGER.info("Registering Mod Items for " + MixedStacks.MOD_ID);

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(MIXED_STACK);
        });
    }
}