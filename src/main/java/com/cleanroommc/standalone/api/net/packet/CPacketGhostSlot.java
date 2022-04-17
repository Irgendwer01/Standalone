package com.cleanroommc.standalone.api.net.packet;

import com.cleanroommc.standalone.api.net.IPacket;
import com.cleanroommc.standalone.client.gui.widget.GhostSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;

public class CPacketGhostSlot implements IPacket {

    private int slot;
    private ItemStack stack;
    private int size;
    private int windowId;

    public CPacketGhostSlot() {

    }

    public CPacketGhostSlot(int slot, @Nonnull ItemStack stack, int size) {
        this.slot = slot;
        this.stack = stack;
        this.size = size;
        this.windowId = Minecraft.getMinecraft().player.openContainer.windowId;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeInt(slot);
        buf.writeItemStack(stack);
        buf.writeInt(size);
    }

    @Override
    public void decode(@Nonnull PacketBuffer buf) {
        this.windowId = buf.readInt();
        this.slot = buf.readInt();
        try {
            this.stack = buf.readItemStack();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.size = buf.readInt();
    }

    @Override
    public void executeServer(@Nonnull NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof GhostSlot.IGhostSlotAware && container.windowId == windowId) {
            ((GhostSlot.IGhostSlotAware) container).setGhostSlotContents(slot, stack, size);
        }
    }
}
