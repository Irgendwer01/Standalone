package com.cleanroommc.standalone.api.tileentity;

import com.google.common.base.Preconditions;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.cleanroommc.standalone.api.StandaloneDataCodes.INITIALIZE_TE;

public class StandaloneTileEntity extends TickableTileEntityBase {

    private final String name;

    private boolean needToUpdateLightning = false;

    private final int[] sidedRedstoneOutput = new int[6];
    private final int[] sidedRedstoneInput = new int[6];
    private int cachedComparatorValue;
    private int cachedLightValue;

    public StandaloneTileEntity(String name) {
        this.name = name;
    }

    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (getOffsetTimer() % 5 == 0)
                updateComparatorValue();
        }
        if (getOffsetTimer() % 5 == 0)
            updateLightValue();

        if (this.needToUpdateLightning) {
            getWorld().checkLight(getPos());
            this.needToUpdateLightning = false;
        }
        //increment only after current tick, so meta tile entities will get first tick as timer == 0
        //and update their settings which depend on getTimer() % N properly
        super.update();
    }

    @Override
    protected void onFirstTick() {

    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("CachedLightValue", cachedLightValue);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.cachedLightValue = compound.getInteger("CachedLightValue");
    }

    public void sendInitialSyncData() {

    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeString(getName());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        scheduleChunkForRenderUpdate();
        this.needToUpdateLightning = true;
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buffer) {
        if (discriminator == INITIALIZE_TE) {
            scheduleChunkForRenderUpdate();
            this.needToUpdateLightning = true;
        }
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        onUnload();
    }

    @Override
    public void onLoad() {
        this.cachedComparatorValue = getActualComparatorValue();
        for (EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneInput[side.getIndex()] = getRedstonePower(getWorld(), getPos(), side);
        }
    }

    public void onUnload() {

    }

    public final boolean canConnectRedstone(@Nullable EnumFacing side) {
        //so far null side means either upwards or downwards redstone wire connection
        //so check both top cover and bottom cover
        if (side == null)
            return canConnectRedstone(EnumFacing.UP) || canConnectRedstone(EnumFacing.DOWN);

        return canMachineConnectRedstone(side);
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return false;
    }


    public final int getInputRedstoneSignal(@Nonnull EnumFacing side) {
        return sidedRedstoneInput[side.getIndex()];
    }

    public final boolean isBlockRedstonePowered() {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (getInputRedstoneSignal(side) > 0) {
                return true;
            }
        }
        return false;
    }

    public void onNeighborChanged() {

    }

    public void updateInputRedstoneSignals() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int redstoneValue = getRedstonePower(getWorld(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.getIndex()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.getIndex()] = redstoneValue;
            }
        }
    }

    public static int getRedstonePower(@Nonnull World world, @Nonnull BlockPos blockPos, EnumFacing side) {
        BlockPos offsetPos = blockPos.offset(side);
        int worldPower = world.getRedstonePower(offsetPos, side);
        if (worldPower < 15) {
            IBlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() instanceof BlockRedstoneWire) {
                int wirePower = offsetState.getValue(BlockRedstoneWire.POWER);
                return Math.max(worldPower, wirePower);
            }
        }
        return worldPower;
    }

    public int getActualComparatorValue() {
        return 0;
    }

    public int getActualLightValue() {
        return 0;
    }

    public final int getComparatorValue() {
        return cachedComparatorValue;
    }

    public final int getLightValue() {
        return cachedLightValue;
    }

    private void updateComparatorValue() {
        int newComparatorValue = getActualComparatorValue();
        if (cachedComparatorValue != newComparatorValue) {
            this.cachedComparatorValue = newComparatorValue;
            if (!getWorld().isRemote) {
                notifyBlockUpdate();
            }
        }
    }

    private void updateLightValue() {
        int newLightValue = getActualLightValue();
        if (cachedLightValue != newLightValue) {
            this.cachedLightValue = newLightValue;
            getWorld().checkLight(getPos());
        }
    }

    /**
     * Add special drops which this tile entity contains here
     * Meta tile entity item is ALREADY added into this list
     * Do NOT add inventory contents in this list - it will be dropped automatically when breakBlock is called
     * This will only be called if meta tile entity is broken with proper tool (i.e wrench)
     *
     * @param dropsList list of meta tile entity drops
     * @param harvester harvester of this meta tile entity, or null
     */
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable EntityPlayer harvester) {

    }

    public final int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        return sidedRedstoneOutput[side.getIndex()];
    }

    public final int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            highestSignal = Math.max(highestSignal, sidedRedstoneOutput[side.getIndex()]);
        }
        return highestSignal;
    }

    public final void setOutputRedstoneSignal(EnumFacing side, int strength) {
        Preconditions.checkNotNull(side, "side");
        this.sidedRedstoneOutput[side.getIndex()] = strength;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            markDirty();
        }
    }
}
