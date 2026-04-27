package io.github.lefoxxy.autolatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

final class AutoLatchConfigTest {
    @Test
    void configDefaultsMatchExpectedValues() {
        assertTrue(AutoLatchConfig.ENABLED.getDefault());
        assertEquals(60, AutoLatchConfig.CLOSE_DELAY_TICKS.getDefault());
        assertTrue(AutoLatchConfig.ALLOW_WOODEN_DOORS.getDefault());
        assertFalse(AutoLatchConfig.ALLOW_IRON_DOORS.getDefault());
        assertTrue(AutoLatchConfig.ALLOW_MODDED_DOORS.getDefault());
        assertTrue(AutoLatchConfig.RESPECT_REDSTONE_POWER.getDefault());
        assertFalse(AutoLatchConfig.RETRY_IF_BLOCKED.getDefault());
        assertEquals(40, AutoLatchConfig.RETRY_DELAY_TICKS.getDefault());
        assertEquals(1, AutoLatchConfig.MAX_RETRIES.getDefault());
        assertFalse(AutoLatchConfig.WHITELIST_MODE.getDefault());
        assertEquals(List.of(), AutoLatchConfig.DOOR_WHITELIST.getDefault());
        assertEquals(List.of("minecraft:iron_door"), AutoLatchConfig.DOOR_BLACKLIST.getDefault());
    }
}
