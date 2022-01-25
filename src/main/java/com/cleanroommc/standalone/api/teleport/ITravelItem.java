package com.cleanroommc.standalone.api.teleport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface ITravelItem {

    boolean isActive(@Nonnull EntityPlayer ep, @Nonnull ItemStack equipped);

    void extractInternal(@Nonnull ItemStack item, int power);

    int getEnergyStored(@Nonnull ItemStack item);

}
