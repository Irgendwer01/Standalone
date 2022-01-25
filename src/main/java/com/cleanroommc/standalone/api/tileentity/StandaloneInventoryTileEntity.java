package com.cleanroommc.standalone.api.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IInteractionObject;

import javax.annotation.Nonnull;

public abstract class StandaloneInventoryTileEntity extends StandaloneTileEntity implements IInventory, IInteractionObject {

    private final int size;
    private final NonNullList<ItemStack> inventory;

    public int playerUsingCount = 0;

    public StandaloneInventoryTileEntity(String name, int size) {
        super(name);
        this.size = size;
        inventory = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (!stack.isItemEqual(ItemStack.EMPTY))
                return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        if (index > size || index < 0)
            return ItemStack.EMPTY;

        return getItems().get(index);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.getItems(), index, count);
        if (!itemstack.isEmpty())
            this.markDirty();

        return itemstack;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.getItems(), index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        this.getItems().set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit())
            stack.setCount(this.getInventoryStackLimit());

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this)
            return false;
        else
            return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
        playerUsingCount++;
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
        playerUsingCount--;
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.getItems().clear();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Nonnull
    @Override
    public abstract Container createContainer(@Nonnull InventoryPlayer playerInventory, @Nonnull EntityPlayer playerIn);

    @Nonnull
    @Override
    public abstract String getGuiID();

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, inventory);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        ItemStackHelper.loadAllItems(compound, inventory);
    }
}
