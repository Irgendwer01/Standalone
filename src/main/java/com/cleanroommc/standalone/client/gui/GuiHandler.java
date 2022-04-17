package com.cleanroommc.standalone.client.gui;

import com.cleanroommc.standalone.common.container.TravelAnchorContainer;
import com.cleanroommc.standalone.common.container.TravelAnchorGui;
import com.cleanroommc.standalone.common.tileentity.travelanchor.TileEntityTravelAnchor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_TRAVEL_ANCHOR = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, @Nonnull World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null)
            return null;

        switch (id) {
            case GUI_TRAVEL_ANCHOR:
                return new TravelAnchorContainer(player.inventory, (TileEntityTravelAnchor) tileEntity, player);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, @Nonnull World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null)
            return null;

        switch (id) {
            case GUI_TRAVEL_ANCHOR:
                return new TravelAnchorGui(player.inventory, tileEntity, player);
            default:
                return null;
        }
    }
}
