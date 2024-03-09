package com.cleanroommc.standalone.proxy;

import com.cleanroommc.standalone.Tags;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class TickTimer {

    private static long serverTickCount = 0;
    private static long clientTickCount = 0;
    private static long clientPausedTickCount = 0;

    @SubscribeEvent
    public static void onTick(@Nonnull TickEvent.ServerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            ++serverTickCount;
        }
    }

    @SubscribeEvent
    public static void onTick(@Nonnull TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            if (Minecraft.getMinecraft().isGamePaused()) {
                ++clientPausedTickCount;
            } else {
                ++clientTickCount;
            }
        }
    }

    public static long getServerTickCount() {
        return serverTickCount;
    }

    public static long getClientTickCount() {
        return clientTickCount;
    }

    public static long getClientPausedTickCount() {
        return clientPausedTickCount;
    }
}

