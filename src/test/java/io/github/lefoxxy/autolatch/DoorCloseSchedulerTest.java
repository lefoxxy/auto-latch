package io.github.lefoxxy.autolatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

final class DoorCloseSchedulerTest {
    @Test
    void duplicateEntriesForSameDimensionAndPositionAreRejected() {
        DoorCloseScheduler scheduler = new DoorCloseScheduler();
        BlockPos pos = new BlockPos(1, 64, 1);
        ResourceKey<Level> dimension = dimension("minecraft:overworld");

        assertTrue(scheduler.schedule(new DoorCloseScheduler.ScheduledDoorClose(dimension, pos, 60L, 0)));
        assertFalse(scheduler.schedule(new DoorCloseScheduler.ScheduledDoorClose(dimension, pos, 80L, 0)));
        assertEquals(1, scheduler.pendingCount());
    }

    @Test
    void samePositionInDifferentDimensionsCanBeScheduled() {
        DoorCloseScheduler scheduler = new DoorCloseScheduler();
        BlockPos pos = new BlockPos(1, 64, 1);

        assertTrue(scheduler.schedule(new DoorCloseScheduler.ScheduledDoorClose(dimension("minecraft:overworld"), pos, 60L, 0)));
        assertTrue(scheduler.schedule(new DoorCloseScheduler.ScheduledDoorClose(dimension("autolatch:test"), pos, 60L, 0)));
        assertEquals(2, scheduler.pendingCount());
    }

    @Test
    void scheduledEntriesStoreRetryCount() {
        DoorCloseScheduler.ScheduledDoorClose close =
                new DoorCloseScheduler.ScheduledDoorClose(dimension("minecraft:overworld"), BlockPos.ZERO, 60L, 0);

        assertEquals(0, close.retryCount());
    }

    @Test
    void retryIncrementsCountAndUsesRetryDelay() {
        DoorCloseScheduler scheduler = new DoorCloseScheduler();
        DoorCloseScheduler.ScheduledDoorClose close =
                new DoorCloseScheduler.ScheduledDoorClose(dimension("minecraft:overworld"), BlockPos.ZERO, 60L, 0);

        assertTrue(scheduler.scheduleRetry(close, 100L, 40, 1));

        DoorCloseScheduler.ScheduledDoorClose retry = scheduler.peekNextForTesting();
        assertEquals(140L, retry.targetGameTime());
        assertEquals(1, retry.retryCount());
        assertEquals(1, scheduler.pendingCount());
    }

    @Test
    void retryCountIsCapped() {
        DoorCloseScheduler scheduler = new DoorCloseScheduler();
        DoorCloseScheduler.ScheduledDoorClose close =
                new DoorCloseScheduler.ScheduledDoorClose(dimension("minecraft:overworld"), BlockPos.ZERO, 60L, 1);

        assertFalse(scheduler.scheduleRetry(close, 100L, 40, 1));
        assertEquals(0, scheduler.pendingCount());
    }

    @Test
    void retryDoesNotCreateDuplicateWhileEntryExists() {
        DoorCloseScheduler scheduler = new DoorCloseScheduler();
        ResourceKey<Level> dimension = dimension("minecraft:overworld");
        DoorCloseScheduler.ScheduledDoorClose existing =
                new DoorCloseScheduler.ScheduledDoorClose(dimension, BlockPos.ZERO, 100L, 1);
        DoorCloseScheduler.ScheduledDoorClose close =
                new DoorCloseScheduler.ScheduledDoorClose(dimension, BlockPos.ZERO, 60L, 0);

        assertTrue(scheduler.schedule(existing));
        assertFalse(scheduler.scheduleRetry(close, 100L, 40, 2));
        assertEquals(1, scheduler.pendingCount());
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<Level> dimension(String id) {
        try {
            Constructor<ResourceKey> constructor =
                    ResourceKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            return (ResourceKey<Level>) constructor.newInstance(
                    ResourceLocation.tryParse("minecraft:dimension"),
                    ResourceLocation.tryParse(id));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
