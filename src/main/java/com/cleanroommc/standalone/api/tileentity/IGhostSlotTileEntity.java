package com.cleanroommc.standalone.api.tileentity;

import net.minecraft.item.ItemStack;

public interface IGhostSlotTileEntity {

    void setGhostSlotContents(int slot, ItemStack stack, int size);
}
