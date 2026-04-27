package io.github.lefoxxy.autolatch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class DoorLatchHandler {
    private final DoorCloseScheduler closeScheduler = new DoorCloseScheduler();

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AutoLatchConfig.ENABLED.get() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos().immutable();
        BlockState clickedState = level.getBlockState(pos);
        if (!isEligibleDoor(clickedState)) {
            return;
        }

        level.getServer().execute(() -> confirmOpenedDoor(level, pos));
    }

    private void confirmOpenedDoor(ServerLevel level, BlockPos pos) {
        if (!AutoLatchConfig.ENABLED.get()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (!isEligibleDoor(state) || !state.hasProperty(BlockStateProperties.OPEN)) {
            return;
        }

        if (state.getValue(BlockStateProperties.OPEN)) {
            onDoorOpened(level, pos, state);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!AutoLatchConfig.ENABLED.get() || event.phase != TickEvent.Phase.END) {
            return;
        }

        closeScheduler.process(event.getServer());
    }

    private void onDoorOpened(ServerLevel level, BlockPos pos, BlockState state) {
        closeScheduler.schedule(level, pos, AutoLatchConfig.CLOSE_DELAY_TICKS.get());
    }

    private boolean isEligibleDoor(BlockState state) {
        if (!(state.getBlock() instanceof DoorBlock)) {
            return false;
        }

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return DoorEligibility.isAllowed(id);
    }
}
