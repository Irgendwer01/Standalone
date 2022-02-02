package com.cleanroommc.standalone.common.blocks.travelanchor;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneBlock;
import com.cleanroommc.standalone.api.blockowner.UserIdentification;
import com.cleanroommc.standalone.api.teleport.ITravelAccessible;
import com.cleanroommc.standalone.client.gui.GuiHandler;
import com.cleanroommc.standalone.client.render.ITESRBlock;
import com.cleanroommc.standalone.client.render.renderers.TravelSpecialRenderer;
import com.cleanroommc.standalone.common.tileentity.travelanchor.TileEntityTravelAnchor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTravelAnchor extends StandaloneBlock implements ITileEntityProvider, ITESRBlock {

    public BlockTravelAnchor() {
        super(new BlockSettings(Material.IRON)
                .strength(2.5f)
                .soundType(SoundType.STONE)
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
        return new TileEntityTravelAnchor(); //todo this isn't getting set in world
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityTravelAnchor && placer instanceof EntityPlayer) {
            TileEntityTravelAnchor anchor = (TileEntityTravelAnchor) tileEntity;
            anchor.setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ITravelAccessible))
            return false;

        ITravelAccessible travelAccessible = (ITravelAccessible) te;

        if (travelAccessible.getOwner().equals(UserIdentification.create(player.getGameProfile()))
                || (travelAccessible.getAccessMode() == ITravelAccessible.AccessMode.PUBLIC)
                || (player.isCreative() && !willHarvest)) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        } else {
            if (!world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation("standalone.ownership.blockowner", travelAccessible.getOwner().getPlayerName()), true);
            }
        }
        return false;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState bs) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bindTileEntitySpecialRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTravelAnchor.class, new TravelSpecialRenderer<>());
    }
}
