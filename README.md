# Mixed Stacks

We've all been here before. You have 2 of basically the same item (e.g. charcoal and coal), but they take up 2 different inventory slots? Introducing... **Mixed Stacks!**

![A player's inventory, containing a Mixed Stack with Cooked Beef and Cooked Porkchop.](https://cdn.modrinth.com/data/cached_images/9dff618c393e5330d467678a4536468a0f3323df.png)

## How They Work

1. Grab an item from your inventory.
2. Right-click on another item.
3. If it allows them to combine, they'll combine into one inventory slot!


<details>
<summary>Advanced Info</summary>
The default config looks like this:

```json
{
  "tagKeywords": [
    "logs",
    "wool",
    "planks",
    "stone_bricks",
    "concrete",
    "glass",
    "carpets",
    "terracotta",
    "beds",
    "banners",
    "candles",
    "shulker_boxes",
    "stairs",
    "slabs",
    "walls",
    "fences",
    "fence_gates",
    "doors",
    "trapdoors",
    "buttons",
    "pressure_plates",
    "signs",
    "boats",
    "flowers",
    "saplings",
    "leaves",
    "crops",
    "seeds",
    "vegetables",
    "fruits",
    "ores",
    "raw_materials",
    "ingots",
    "dusts",
    "plates",
    "gears"
  ],
  "compatibleItemPairs": [
    [
      "minecraft:beef",
      "minecraft:porkchop"
    ],
    [
      "minecraft:cooked_beef",
      "minecraft:cooked_porkchop"
    ],
    [
      "minecraft:chicken",
      "minecraft:rabbit"
    ],
    [
      "minecraft:cooked_chicken",
      "minecraft:cooked_rabbit"
    ],
    [
      "minecraft:sand",
      "minecraft:red_sand"
    ],
    [
      "minecraft:sandstone",
      "minecraft:red_sandstone"
    ],
    [
      "minecraft:dirt",
      "minecraft:coarse_dirt"
    ],
    [
      "minecraft:brown_mushroom",
      "minecraft:red_mushroom"
    ],
    [
      "minecraft:crimson_fungus",
      "minecraft:warped_fungus"
    ],
    [
      "ae2:certus_quartz_crystal",
      "ae2:charged_certus_quartz_crystal"
    ]
  ]
}
```

You can add specific item pairs via `compatibleItemPairs`, or specify tags that can combine using `tagKeywords`.
</details>


If you want items from your favorite mod added, you can do so [here](https://github.com/LogicalMeatbag/mixed-stacks/issues/new?template=default-config-request.md).

Also on [Modrinth](https://modrinth.com/mod/mixed-stacks) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mixed-stacks), if that's your thing.
