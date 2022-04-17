package com.cleanroommc.standalone.common.container;

import com.cleanroommc.standalone.api.net.NetworkHandler;
import com.cleanroommc.standalone.api.net.packet.CPacketGhostSlot;
import com.cleanroommc.standalone.api.teleport.ITravelAccessible;
import com.cleanroommc.standalone.api.tileentity.IGhostSlotTileEntity;
import com.cleanroommc.standalone.client.gui.widget.GhostSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TravelAnchorContainer extends Container implements GhostSlot.IGhostSlotAware {

    private final IInventory inventory;

    public TravelAnchorContainer(IInventory playerInv, @Nonnull IInventory inventory, EntityPlayer player) {
        this.inventory = inventory;
        inventory.openInventory(player);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 109));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return inventory.isUsableByPlayer(playerIn);
    }

    @Override
    public void setGhostSlotContents(int slot, ItemStack stack, int size) {
        if (this.inventory instanceof IGhostSlotTileEntity) {
            ((IGhostSlotTileEntity) this.inventory).setGhostSlotContents(slot, stack, size);
        }
    }

    public static class TravelAnchorGhostSlot extends GhostSlot {

        private final ITravelAccessible travelAccessible;

        public TravelAnchorGhostSlot(@Nonnull ITravelAccessible travelAccessible, int slotIndex, int x, int y) {
            this.setSlot(slotIndex);
            this.setX(x);
            this.setY(y);
            this.setDisplayStandardOverlay(false);
            this.setGrayOut(false);
            this.setStackSizeLimit(1);
            this.travelAccessible = travelAccessible;
        }

        @Override
        public @Nonnull
        ItemStack getStack() {
            return travelAccessible.getItemLabel();
        }

        @Override
        public void putStack(@Nonnull ItemStack stack, int size) {
            // client
            travelAccessible.setItemLabel(stack);
            // server
            NetworkHandler.channel.sendToServer(new CPacketGhostSlot(getSlot(), stack, size).toFMLPacket());
        }
    }
}
