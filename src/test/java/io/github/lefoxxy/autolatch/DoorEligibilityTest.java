package io.github.lefoxxy.autolatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class DoorEligibilityTest {
    @Test
    void oakDoorAllowedByDefault() {
        assertTrue(defaultAllowed("minecraft:oak_door"));
    }

    @Test
    void spruceDoorAllowedByDefault() {
        assertTrue(defaultAllowed("minecraft:spruce_door"));
    }

    @Test
    void ironDoorBlockedByDefault() {
        assertFalse(defaultAllowed("minecraft:iron_door"));
    }

    @Test
    void blacklistOverridesWhitelist() {
        assertFalse(DoorEligibility.isAllowed(
                id("minecraft:oak_door"),
                true,
                true,
                true,
                true,
                List.of("minecraft:oak_door"),
                List.of("minecraft:oak_door")));
    }

    @Test
    void whitelistModeOnlyAllowsListedIds() {
        assertTrue(DoorEligibility.isAllowed(
                id("minecraft:oak_door"),
                true,
                true,
                true,
                true,
                List.of("minecraft:oak_door"),
                List.of()));

        assertFalse(DoorEligibility.isAllowed(
                id("minecraft:spruce_door"),
                true,
                true,
                true,
                true,
                List.of("minecraft:oak_door"),
                List.of()));
    }

    @Test
    void doorKindsMatchRequestedClassification() {
        assertSame(DoorEligibility.DoorKind.WOODEN, DoorEligibility.classify(id("minecraft:oak_door")));
        assertSame(DoorEligibility.DoorKind.WOODEN, DoorEligibility.classify(id("minecraft:spruce_door")));
        assertSame(DoorEligibility.DoorKind.IRON, DoorEligibility.classify(id("minecraft:iron_door")));
        assertSame(DoorEligibility.DoorKind.MODDED, DoorEligibility.classify(id("examplemod:steel_door")));
    }

    @Test
    void kindGatesAreApplied() {
        assertFalse(DoorEligibility.isAllowed(id("minecraft:oak_door"), false, true, true, false, List.of(), List.of()));
        assertFalse(DoorEligibility.isAllowed(id("minecraft:iron_door"), true, false, true, false, List.of(), List.of()));
        assertFalse(DoorEligibility.isAllowed(id("examplemod:steel_door"), true, true, false, false, List.of(), List.of()));
    }

    private static boolean defaultAllowed(String id) {
        return DoorEligibility.isAllowed(
                id(id),
                true,
                false,
                true,
                false,
                List.of(),
                List.of("minecraft:iron_door"));
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.tryParse(value);
    }
}
