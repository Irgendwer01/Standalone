package com.cleanroommc.standalone.api.net.packet;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.net.IPacket;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class CPacketOpenAuthGui implements IPacket {

    public static final int GUI_ID_TRAVEL_AUTH = 42;

    private BlockPos pos;

    public CPacketOpenAuthGui() {

    }

    public CPacketOpenAuthGui(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(@Nonnull PacketBuffer buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void executeServer(@Nonnull NetHandlerPlayServer handler) {
        handler.player.openGui(Standalone.INSTANCE, GUI_ID_TRAVEL_AUTH, handler.player.world, pos.getX(), pos.getY(), pos.getZ());
    }
}
