package com.cleanroommc.standalone.api.teleport;

import com.cleanroommc.standalone.api.blockowner.UserIdentification;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITTravelAccessible {

    enum AccessMode {
        PUBLIC,
        PRIVATE,
        PROTECTED
    }

    boolean canBlockBeAccessed(@Nonnull EntityPlayer playerName);

    boolean canSeeBlock(@Nonnull EntityPlayer playerName);

    boolean canUiBeAccessed(@Nonnull EntityPlayer username);

    boolean getRequiresPassword(@Nonnull EntityPlayer username);

    boolean authoriseUser(@Nonnull EntityPlayer username, @Nonnull ItemStack[] password);

    @Nonnull
    AccessMode getAccessMode();

    void setAccessMode(@Nonnull AccessMode accessMode);

    @Nonnull
    NonNullList<ItemStack> getPassword();

    void setPassword(@Nonnull NonNullList<ItemStack> password);

    @Nonnull
    ItemStack getItemLabel();

    void setItemLabel(@Nonnull ItemStack labelIcon);

    @Nullable
    String getLabel();

    void setLabel(@Nullable String label);

    @Nonnull
    UserIdentification getOwner();

    void clearAuthorisedUsers();

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
