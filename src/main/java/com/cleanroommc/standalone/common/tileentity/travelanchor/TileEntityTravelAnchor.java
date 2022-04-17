package com.cleanroommc.standalone.common.tileentity.travelanchor;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.teleport.ITravelAccessible;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.api.tileentity.IGhostSlotTileEntity;
import com.cleanroommc.standalone.api.tileentity.StandaloneInventoryTileEntity;
import com.cleanroommc.standalone.common.container.TravelAnchorContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TileEntityTravelAnchor extends StandaloneInventoryTileEntity implements ITravelAccessible, IGhostSlotTileEntity {

    private ItemStack itemLabel = ItemStack.EMPTY;
    private String label = "";
    private boolean visible = true;

    public TileEntityTravelAnchor() {
        super("travel_anchor", 27);
    }

    @Nonnull
    @Override
    public Container createContainer(@Nonnull InventoryPlayer playerInventory, @Nonnull EntityPlayer playerIn) {
        return new TravelAnchorContainer(playerInventory, this, playerIn);
    }

    @Nonnull
    @Override
    public String getGuiID() {
        return Standalone.MODID + ":travel_anchor";
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("label", label);
        compound.setTag("itemLabel", itemLabel.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.label = compound.getString("label");
        this.itemLabel = new ItemStack(compound.getCompoundTag("itemLabel"));
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeString(label);
        buf.writeItemStack(itemLabel);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.label = buf.readString(35);
        try {
            this.itemLabel = buf.readItemStack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public ItemStack getItemLabel() {
        return itemLabel;
    }

    @Override
    public void setItemLabel(@Nonnull ItemStack labelIcon) {
        this.itemLabel = labelIcon;
    }

    @Nonnull
    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(@Nonnull String label) {
        this.label = label;
    }

    @Nonnull
    @Override
    public BlockPos getLocation() {
        return getPos();
    }

    @Override
    public int getTravelRangeDeparting() {
        return TravelSource.BLOCK.getMaxDistanceTravelled();
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setGhostSlotContents(int slot, ItemStack stack, int size) {
        setItemLabel(stack);
    }
}
