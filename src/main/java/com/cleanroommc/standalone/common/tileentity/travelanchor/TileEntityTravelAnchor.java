package com.cleanroommc.standalone.common.tileentity.travelanchor;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.common.container.TravelAnchorContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class TileEntityTravelAnchor extends TileEntityLockableLoot {

    public int playerUsingCount = 0;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory)
            if (!itemstack.isEmpty())
                return false;

        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
        playerUsingCount++;
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
        playerUsingCount--;
    }

    @Nonnull
    @Override
    protected NonNullList<ItemStack> getItems() {
        return inventory;
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
    public String getName() {
        return "container.travel_anchor";
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!checkLootAndRead(compound))
            ItemStackHelper.loadAllItems(compound, inventory);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!checkLootAndWrite(compound))
            ItemStackHelper.saveAllItems(compound, inventory);
        return compound;
    }
}
