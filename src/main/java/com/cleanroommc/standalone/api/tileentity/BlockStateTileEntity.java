package com.cleanroommc.standalone.api.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

public abstract class BlockStateTileEntity extends TileEntity {

    @SuppressWarnings("deprecation")
    public IBlockState getBlockState() {
        return getBlockType().getStateFromMeta(getBlockMetadata());
    }
}
