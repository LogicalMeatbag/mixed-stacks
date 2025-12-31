package com.anahkenonje.mixedstacks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MixedStacksConfig {

    public static MixedStacksConfig INSTANCE;
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "mixed-stacks.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- Options ---
    public boolean allowFoodMixing = true;
    public boolean allowAllItems = false;

    public List<String> tagKeywords = new ArrayList<>();
    public List<List<String>> compatibleItemPairs = new ArrayList<>(); // Restored nested list structure

    // --- Logic ---
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, MixedStacksConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
                INSTANCE = new MixedStacksConfig();
            }
        } else {
            INSTANCE = new MixedStacksConfig();
            INSTANCE.loadDefaults(); // Load the big list of defaults
            save();
        }
    }

    public void loadDefaults() {
        // 1. Tag Keywords
        tagKeywords.addAll(Arrays.asList(
                "logs", "wool", "planks", "stone_bricks", "concrete", "glass", "carpets",
                "terracotta", "beds", "banners", "candles", "shulker_boxes", "stairs",
                "slabs", "walls", "fences", "fence_gates", "doors", "trapdoors", "buttons",
                "pressure_plates", "signs", "boats", "flowers", "saplings", "leaves",
                "crops", "seeds", "vegetables", "fruits", "ores", "raw_materials",
                "ingots", "dusts", "plates", "gears"
        ));

        // 2. Compatible Pairs
        addPair("minecraft:beef", "minecraft:porkchop");
        addPair("minecraft:cooked_beef", "minecraft:cooked_porkchop");
        addPair("minecraft:chicken", "minecraft:rabbit");
        addPair("minecraft:cooked_chicken", "minecraft:cooked_rabbit");
        addPair("minecraft:sand", "minecraft:red_sand");
        addPair("minecraft:sandstone", "minecraft:red_sandstone");
        addPair("minecraft:dirt", "minecraft:coarse_dirt");
        addPair("minecraft:brown_mushroom", "minecraft:red_mushroom");
        addPair("minecraft:crimson_fungus", "minecraft:warped_fungus");
        addPair("ae2:certus_quartz_crystal", "ae2:charged_certus_quartz_crystal");
    }

    private void addPair(String a, String b) {
        List<String> pair = new ArrayList<>();
        pair.add(a);
        pair.add(b);
        compatibleItemPairs.add(pair);
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean canMix(ItemStack stackA, ItemStack stackB) {
        if (stackA.isEmpty() || stackB.isEmpty()) return true;
        if (allowAllItems) return true;

        // 1. Food Check
        if (allowFoodMixing && stackA.has(net.minecraft.core.component.DataComponents.FOOD) && stackB.has(net.minecraft.core.component.DataComponents.FOOD)) {
            return true;
        }

        String idA = BuiltInRegistries.ITEM.getKey(stackA.getItem()).toString();
        String idB = BuiltInRegistries.ITEM.getKey(stackB.getItem()).toString();

        // 2. Pair Check (Iterate the nested lists)
        for (List<String> pair : compatibleItemPairs) {
            if (pair.size() >= 2) {
                if ((pair.contains(idA) && pair.contains(idB))) {
                    return true;
                }
            }
        }

        // 3. Tag/Keyword Check
        for (String keyword : tagKeywords) {
            if (idA.contains(keyword) && idB.contains(keyword)) {
                return true;
            }
        }

        return stackA.getItem() == stackB.getItem();
    }
}