package com.cleanroommc.standalone.common.blocks.travelanchor;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneBlock;
import com.cleanroommc.standalone.client.gui.GuiHandler;
import com.cleanroommc.standalone.common.tileentity.travelanchor.TileEntityTravelAnchor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTravelAnchor extends StandaloneBlock implements ITileEntityProvider {

    public BlockTravelAnchor() {
        super(new BlockSettings(Material.IRON)
                .strength(2.5f)
                .soundType(SoundType.ANVIL)
                .creativeTab(CreativeTabs.TRANSPORTATION)
                .translationKey("travel_anchor")
        );
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
                                    @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote)
            playerIn.openGui(Standalone.INSTANCE, GuiHandler.GUI_TRAVEL_ANCHOR, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean eventReceived(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, int id, int param) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityTravelAnchor();
    }
}
