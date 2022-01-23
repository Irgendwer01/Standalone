package com.cleanroommc.standalone.proxy;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.client.gui.GuiHandler;
import com.cleanroommc.standalone.client.resource.StandaloneSounds;
import com.cleanroommc.standalone.client.resource.StandaloneTextures;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(Standalone.INSTANCE, new GuiHandler());
        StandaloneSounds.initSounds();
        StandaloneTextures.initTextures();
    }


    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        //noinspection ConstantConditions
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
    }
}
