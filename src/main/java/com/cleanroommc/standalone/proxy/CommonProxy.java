package com.cleanroommc.standalone.proxy;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.net.NetworkHandler;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod.EventBusSubscriber(modid = Standalone.MODID)
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
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
}
