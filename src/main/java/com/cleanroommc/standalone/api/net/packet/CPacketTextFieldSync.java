package com.cleanroommc.standalone.api.net.packet;

import com.cleanroommc.standalone.api.net.IPacket;
import com.cleanroommc.standalone.api.teleport.ITravelAccessible;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class CPacketTextFieldSync implements IPacket {

    private int maxTextLength;

    private String text;
    private BlockPos pos;

    public CPacketTextFieldSync() {
        this.maxTextLength = 0;
    }

    public CPacketTextFieldSync(@Nonnull String text, @Nonnull BlockPos pos, int maxTextLength) {
        this.text = text;
        this.pos = pos;
        this.maxTextLength = maxTextLength;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buf) {
        buf.writeInt(maxTextLength);
        buf.writeString(text);
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(@Nonnull PacketBuffer buf) {
        this.maxTextLength = buf.readInt();
        this.text = buf.readString(maxTextLength);
        this.pos = buf.readBlockPos();
    }

    @Override
    public void executeServer(@Nonnull NetHandlerPlayServer handler) {
        TileEntity tileEntity = handler.player.world.getTileEntity(pos);
        if (tileEntity instanceof ITravelAccessible) {
            ((ITravelAccessible) tileEntity).setLabel(text);
        }
    }
}
