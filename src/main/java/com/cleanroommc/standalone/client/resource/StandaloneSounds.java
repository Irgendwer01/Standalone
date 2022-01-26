package com.cleanroommc.standalone.client.resource;

import com.cleanroommc.standalone.Standalone;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class StandaloneSounds {

    public static SoundEvent TRAVEL_SOURCE;

    @SideOnly(Side.CLIENT)
    public static void initSounds() {
        TRAVEL_SOURCE = registerVanillaSound("entity.endermen.teleport");
    }

    @Nonnull
    private static SoundEvent registerSound(@Nonnull String soundNameIn) {
        ResourceLocation location = new ResourceLocation(Standalone.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
    @Nonnull
    private static SoundEvent registerVanillaSound(@Nonnull String soundNameIn) {
        ResourceLocation location = new ResourceLocation("minecraft", soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
