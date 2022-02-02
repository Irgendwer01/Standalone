package com.cleanroommc.standalone.client.render;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITESRBlock {

    @SideOnly(Side.CLIENT)
    void bindTileEntitySpecialRenderer();
}
