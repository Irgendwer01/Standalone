package com.cleanroommc.standalone;

import com.cleanroommc.standalone.common.blocks.StandaloneBlocks;
import com.cleanroommc.standalone.common.items.StandaloneItems;
import com.cleanroommc.standalone.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Mod(modid = Standalone.MODID,
        name = Standalone.NAME,
        version = Standalone.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        dependencies = "required:forge@[14.23.5.2847,);" + "after:airlock")
public class Standalone {

    public static final String MODID = "standalone";
    public static final String NAME = "Standalone";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance
    public static Standalone INSTANCE;

    @SidedProxy(modId = MODID, clientSide = "com.cleanroommc.standalone.proxy.ClientProxy", serverSide = "com.cleanroommc.standalone.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @SubscribeEvent
    public void onBlockRegister(@Nonnull RegistryEvent.Register<Block> event) {
        StandaloneBlocks.registerBlock(event.getRegistry());
    }

    @SubscribeEvent
    public void onItemRegister(@Nonnull RegistryEvent.Register<Item> event) {
        StandaloneItems.registerItem(event.getRegistry());
    }

    @SubscribeEvent
    public void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
    }

    @SubscribeEvent
    public void onItemModelRegister(ModelRegistryEvent event) {
        StandaloneItems.registerModel();
        StandaloneBlocks.registerModel();
    }
}
