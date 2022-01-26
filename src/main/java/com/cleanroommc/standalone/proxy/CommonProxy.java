package com.cleanroommc.standalone.proxy;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.teleport.StandaloneLifecycleEvent;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Standalone.MODID)
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {

    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void registerItemRenderer(Item item, int meta, String id) {

    }

    public long getTickCount() {
        return TickTimer.getServerTickCount();
    }

    public long getServerTickCount() {
        return TickTimer.getServerTickCount();
    }

    public StandaloneLifecycleEvent.ServerStarting getServerStartingEvent(@Nonnull FMLServerStartingEvent event) {
        return new StandaloneLifecycleEvent.ServerStarting.Dedicated(event);
    }
}
