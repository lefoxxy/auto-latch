package io.github.lefoxxy.autolatch;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AutoLatchConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue CLOSE_DELAY_TICKS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_WOODEN_DOORS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_IRON_DOORS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_MODDED_DOORS;
    public static final ForgeConfigSpec.BooleanValue RESPECT_REDSTONE_POWER;
    public static final ForgeConfigSpec.BooleanValue RETRY_IF_BLOCKED;
    public static final ForgeConfigSpec.IntValue RETRY_DELAY_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_RETRIES;
    public static final ForgeConfigSpec.BooleanValue WHITELIST_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DOOR_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DOOR_BLACKLIST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        ENABLED = builder
                .comment("Enables AutoLatch server-side door detection and auto-closing.")
                .define("enabled", true);
        CLOSE_DELAY_TICKS = builder
                .comment("Number of server ticks to wait before AutoLatch closes an eligible opened door.")
                .defineInRange("closeDelayTicks", 60, 1, 20 * 60 * 60);
        ALLOW_WOODEN_DOORS = builder
                .comment("Allows vanilla Minecraft wooden doors, such as oak and spruce doors, to auto-close.")
                .define("allowWoodenDoors", true);
        ALLOW_IRON_DOORS = builder
                .comment("Allows minecraft:iron_door to auto-close. Disabled by default so redstone doors are not changed unexpectedly.")
                .define("allowIronDoors", false);
        ALLOW_MODDED_DOORS = builder
                .comment("Allows non-minecraft DoorBlock instances to auto-close.")
                .define("allowModdedDoors", true);
        RESPECT_REDSTONE_POWER = builder
                .comment("When true, AutoLatch skips closing doors that are powered at close time.")
                .define("respectRedstonePower", true);
        RETRY_IF_BLOCKED = builder
                .comment("When true, a powered door skipped because of redstone can be checked again later.")
                .define("retryIfBlocked", false);
        RETRY_DELAY_TICKS = builder
                .comment("Number of server ticks to wait before retrying a redstone-blocked door.")
                .defineInRange("retryDelayTicks", 40, 1, 20 * 60 * 60);
        MAX_RETRIES = builder
                .comment("Maximum number of redstone-blocked retry attempts per scheduled door.")
                .defineInRange("maxRetries", 1, 0, 100);
        WHITELIST_MODE = builder
                .comment("When true, only doors listed in doorWhitelist can auto-close. doorBlacklist still overrides this list.")
                .define("whitelistMode", false);
        DOOR_WHITELIST = builder
                .comment("Door block ids allowed when whitelistMode is true, for example minecraft:oak_door.")
                .defineListAllowEmpty(List.of("doorWhitelist"), List.of(), AutoLatchConfig::isString);
        DOOR_BLACKLIST = builder
                .comment("Door block ids that AutoLatch never auto-closes. This list always overrides the whitelist.")
                .defineListAllowEmpty(List.of("doorBlacklist"), List.of("minecraft:iron_door"), AutoLatchConfig::isString);
        builder.pop();

        SPEC = builder.build();
    }

    private AutoLatchConfig() {
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }
}
