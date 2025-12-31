package com.anahkenonje.mixedstacks.compat;

import com.anahkenonje.mixedstacks.config.MixedStacksConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MixedStacksConfig config = MixedStacksConfig.INSTANCE;
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("text.autoconfig.mixed-stacks.title"));

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.mixed-stacks.option.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Boolean: Allow Food
            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Allow Mixing Food"), config.allowFoodMixing)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.allowFoodMixing = newValue)
                    .build());

            // Boolean: Allow All
            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Allow All Items (Cheat)"), config.allowAllItems)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> config.allowAllItems = newValue)
                    .build());

            // List: Tag Keywords
            general.addEntry(entryBuilder.startStrList(Component.literal("Matching Keywords"), config.tagKeywords)
                    .setDefaultValue(new ArrayList<>())
                    .setTooltip(Component.literal("Items with IDs containing these words can mix (e.g. 'log', 'wool')"))
                    .setSaveConsumer(newList -> config.tagKeywords = new ArrayList<>(newList))
                    .build());

            // --- SMART PAIR CONVERSION ---
            // 1. Convert List<List<String>> -> List<String> for Display (e.g. "a + b")
            List<String> displayPairs = new ArrayList<>();
            for (List<String> pair : config.compatibleItemPairs) {
                if (pair.size() >= 2) {
                    displayPairs.add(pair.get(0) + " + " + pair.get(1));
                }
            }

            // 2. Create the Entry
            general.addEntry(entryBuilder.startStrList(Component.literal("Custom Pairs"), displayPairs)
                    .setDefaultValue(new ArrayList<>())
                    .setTooltip(Component.literal("Format: item_id + item_id (e.g. minecraft:cooked_beef + minecraft:porkchop)"))
                    .setSaveConsumer(newList -> {
                        // 3. Convert List<String> -> List<List<String>> on Save
                        List<List<String>> newPairs = new ArrayList<>();
                        for (String s : newList) {
                            if (s.contains("+")) {
                                String[] parts = s.split("\\+");
                                if (parts.length >= 2) {
                                    List<String> pair = new ArrayList<>();
                                    pair.add(parts[0].trim());
                                    pair.add(parts[1].trim());
                                    newPairs.add(pair);
                                }
                            }
                        }
                        config.compatibleItemPairs = newPairs;
                    })
                    .build());

            builder.setSavingRunnable(MixedStacksConfig::save);

            return builder.build();
        };
    }
}