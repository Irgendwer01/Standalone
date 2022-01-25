package com.cleanroommc.standalone.common.tileentity.travelanchor;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.blockowner.UserIdentification;
import com.cleanroommc.standalone.api.teleport.ITTravelAccessible;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.api.tileentity.StandaloneInventoryTileEntity;
import com.cleanroommc.standalone.common.container.TravelAnchorContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityTravelAnchor extends StandaloneInventoryTileEntity implements ITTravelAccessible {

    private AccessMode accessMode = AccessMode.PUBLIC;
    private NonNullList<ItemStack> password = NonNullList.withSize(5, ItemStack.EMPTY);
    private final List<UserIdentification> authorisedUsers = new ArrayList<>();
    private UserIdentification travelOwner;

    private ItemStack itemLabel = ItemStack.EMPTY;
    private String label;
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

    private boolean isOwnerUser(UserIdentification identification) {
        return getOwner().equals(identification);
    }

    private boolean isAuthorisedUser(UserIdentification identification) {
        return authorisedUsers.contains(identification);
    }

    @Override
    public boolean canBlockBeAccessed(@Nonnull EntityPlayer playerName) {
        if (accessMode == AccessMode.PUBLIC)
            return true;

        // Covers protected and private access modes
        return isOwnerUser(UserIdentification.create(playerName.getGameProfile())) || isAuthorisedUser(UserIdentification.create(playerName.getGameProfile()));
    }

    @Override
    public boolean canSeeBlock(@Nonnull EntityPlayer playerName) {
        if (accessMode != AccessMode.PRIVATE)
            return true;

        return isOwnerUser(UserIdentification.create(playerName.getGameProfile()));
    }

    @Override
    public boolean canUiBeAccessed(@Nonnull EntityPlayer username) {
        return isOwnerUser(UserIdentification.create(username.getGameProfile()));
    }

    @Override
    public boolean getRequiresPassword(@Nonnull EntityPlayer username) {
        return getAccessMode() == AccessMode.PROTECTED && !canUiBeAccessed(username) && !isAuthorisedUser(UserIdentification.create(username.getGameProfile()));
    }

    private boolean checkPassword(ItemStack[] pwd) {
        if (pwd == null || pwd.length != password.size()) {
            return false;
        }
        for (int i = 0; i < pwd.length; i++) {
            ItemStack pw = password.get(i);
            ItemStack tst = pwd[i];
            if (pw.isEmpty() && !tst.isEmpty()) {
                return false;
            }
            if (!pw.isEmpty()) {
                if (tst.isEmpty() || !ItemStack.areItemStacksEqual(pw, tst)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean authoriseUser(@Nonnull EntityPlayer username, @Nonnull ItemStack[] password) {
        if (checkPassword(password)) {
            authorisedUsers.add(UserIdentification.create(username.getGameProfile()));
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public void setAccessMode(@Nonnull AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getPassword() {
        return password;
    }

    @Override
    public void setPassword(@Nonnull NonNullList<ItemStack> password) {
        this.password = password;
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

    @Nullable
    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
    }

    public void setOwner(@Nonnull EntityPlayer player) {
        this.travelOwner = UserIdentification.create(player.getGameProfile());
    }

    @Nonnull
    @Override
    public UserIdentification getOwner() {
        return travelOwner != null ? travelOwner : UserIdentification.Nobody.NOBODY;
    }

    @Override
    public void clearAuthorisedUsers() {
        authorisedUsers.clear();
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
}
