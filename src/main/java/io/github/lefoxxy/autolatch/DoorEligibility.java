package io.github.lefoxxy.autolatch;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

final class DoorEligibility {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String IRON_DOOR = "iron_door";
    private static final Set<String> VANILLA_WOODEN_DOORS = Set.of(
            "oak_door",
            "spruce_door",
            "birch_door",
            "jungle_door",
            "acacia_door",
            "dark_oak_door",
            "mangrove_door",
            "cherry_door",
            "bamboo_door",
            "crimson_door",
            "warped_door");

    private DoorEligibility() {
    }

    static boolean isAllowed(ResourceLocation id) {
        return isAllowed(
                id,
                AutoLatchConfig.ALLOW_WOODEN_DOORS.get(),
                AutoLatchConfig.ALLOW_IRON_DOORS.get(),
                AutoLatchConfig.ALLOW_MODDED_DOORS.get(),
                AutoLatchConfig.WHITELIST_MODE.get(),
                AutoLatchConfig.DOOR_WHITELIST.get(),
                AutoLatchConfig.DOOR_BLACKLIST.get());
    }

    static boolean isAllowed(
            ResourceLocation id,
            boolean allowWoodenDoors,
            boolean allowIronDoors,
            boolean allowModdedDoors,
            boolean whitelistMode,
            Iterable<? extends String> whitelist,
            Iterable<? extends String> blacklist) {
        String blockId = id.toString().toLowerCase(Locale.ROOT);
        if (containsBlockId(blacklist, blockId)) {
            return false;
        }

        if (whitelistMode && !containsBlockId(whitelist, blockId)) {
            return false;
        }

        return switch (classify(id)) {
            case WOODEN -> allowWoodenDoors;
            case IRON -> allowIronDoors;
            case MODDED -> allowModdedDoors;
            case UNKNOWN -> false;
        };
    }

    static DoorKind classify(ResourceLocation id) {
        if (!MINECRAFT_NAMESPACE.equals(id.getNamespace())) {
            return DoorKind.MODDED;
        }

        if (IRON_DOOR.equals(id.getPath())) {
            return DoorKind.IRON;
        }

        if (VANILLA_WOODEN_DOORS.contains(id.getPath())) {
            return DoorKind.WOODEN;
        }

        return DoorKind.UNKNOWN;
    }

    private static boolean containsBlockId(Iterable<? extends String> values, String blockId) {
        for (String value : values) {
            if (blockId.equals(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    enum DoorKind {
        WOODEN,
        IRON,
        MODDED,
        UNKNOWN
    }
}
