package io.github.lefoxxy.autolatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class DoorCloseScheduler {
    private final Queue<ScheduledDoorClose> pendingCloses = new PriorityQueue<>();
    private final Map<ScheduledDoorKey, ScheduledDoorClose> scheduledByDoor = new HashMap<>();

    boolean schedule(ServerLevel level, BlockPos pos, int delayTicks) {
        long targetGameTime = level.getGameTime() + delayTicks;
        return schedule(new ScheduledDoorClose(level.dimension(), pos.immutable(), targetGameTime, 0));
    }

    boolean schedule(ScheduledDoorClose close) {
        ScheduledDoorKey key = close.key();
        if (scheduledByDoor.containsKey(key)) {
            return false;
        }

        scheduledByDoor.put(key, close);
        pendingCloses.add(close);
        return true;
    }

    void process(MinecraftServer server) {
        long serverGameTime = server.overworld().getGameTime();
        while (!pendingCloses.isEmpty() && pendingCloses.peek().targetGameTime() <= serverGameTime) {
            ScheduledDoorClose close = pendingCloses.poll();
            ScheduledDoorKey key = close.key();
            if (!Objects.equals(scheduledByDoor.remove(key), close)) {
                continue;
            }

            closeDoorIfValid(server, close);
        }
    }

    int pendingCount() {
        return scheduledByDoor.size();
    }

    ScheduledDoorClose peekNextForTesting() {
        return pendingCloses.peek();
    }

    private void closeDoorIfValid(MinecraftServer server, ScheduledDoorClose close) {
        ServerLevel level = server.getLevel(close.dimension());
        if (level == null || !level.hasChunkAt(close.pos())) {
            return;
        }

        BlockState state = level.getBlockState(close.pos());
        if (!(state.getBlock() instanceof DoorBlock door) || !state.hasProperty(BlockStateProperties.OPEN)) {
            return;
        }

        if (!state.getValue(BlockStateProperties.OPEN)) {
            return;
        }

        if (AutoLatchConfig.RESPECT_REDSTONE_POWER.get() && level.hasNeighborSignal(close.pos())) {
            retryIfConfigured(close, level.getGameTime());
            return;
        }

        door.setOpen(null, level, state, close.pos(), false);
    }

    private void retryIfConfigured(ScheduledDoorClose close, long currentGameTime) {
        if (!AutoLatchConfig.RETRY_IF_BLOCKED.get()) {
            return;
        }

        scheduleRetry(close, currentGameTime, AutoLatchConfig.RETRY_DELAY_TICKS.get(), AutoLatchConfig.MAX_RETRIES.get());
    }

    boolean scheduleRetry(ScheduledDoorClose close, long currentGameTime, int retryDelayTicks, int maxRetries) {
        if (close.retryCount() >= maxRetries) {
            return false;
        }

        return schedule(new ScheduledDoorClose(
                close.dimension(),
                close.pos(),
                currentGameTime + retryDelayTicks,
                close.retryCount() + 1));
    }

    record ScheduledDoorClose(ResourceKey<Level> dimension, BlockPos pos, long targetGameTime, int retryCount)
            implements Comparable<ScheduledDoorClose> {
        ScheduledDoorKey key() {
            return new ScheduledDoorKey(dimension, pos);
        }

        @Override
        public int compareTo(ScheduledDoorClose other) {
            return Long.compare(targetGameTime, other.targetGameTime);
        }
    }

    private record ScheduledDoorKey(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
