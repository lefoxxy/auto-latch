package io.github.lefoxxy.autolatch;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(AutoLatch.MOD_ID)
public final class AutoLatch {
    public static final String MOD_ID = "autolatch";

    public AutoLatch() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AutoLatchConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new DoorLatchHandler());
    }
}
