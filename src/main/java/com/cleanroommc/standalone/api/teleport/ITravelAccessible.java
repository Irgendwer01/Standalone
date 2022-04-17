package com.cleanroommc.standalone.api.teleport;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public interface ITravelAccessible {

    @Nonnull
    ItemStack getItemLabel();

    void setItemLabel(@Nonnull ItemStack labelIcon);

    @Nonnull
    String getLabel();

    void setLabel(@Nonnull String label);

    @Nonnull
    BlockPos getLocation();

    /**
     * Is this block a travel source for traveling to other travel anchors?
     */
    default boolean isTravelSource() {
        return true;
    }

    /**
     * Is this block a visible travel target for the staff or a travel anchor?
     */
    default boolean isVisible() {
        return true;
    }

    default void setVisible(boolean visible) {

    }

    /**
     * If this block is used as a travel source, how far is the travel range?
     */
    int getTravelRangeDeparting();
}
