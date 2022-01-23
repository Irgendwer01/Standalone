package com.cleanroommc.standalone.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TravelAnchorContainer extends Container {

    private final IInventory inventory;

    public TravelAnchorContainer(IInventory playerInv, @Nonnull IInventory inventory, EntityPlayer player) {
        this.inventory = inventory;
        inventory.openInventory(player);

        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                addSlotToContainer(new Slot(inventory, j + i * 9, 8 + j * 18, 18 + i * 18));

        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

        for (int i = 0; i < 9; ++i)
            addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            if (index < inventory.getSizeInventory()) {
                if (!mergeItemStack(stack, inventory.getSizeInventory(), inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stack, 0, inventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        inventory.closeInventory(playerIn);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return inventory.isUsableByPlayer(playerIn);
    }
}
