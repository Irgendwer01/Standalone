package com.cleanroommc.standalone.api.net.packet;

import com.cleanroommc.standalone.api.StandaloneBlock;
import com.cleanroommc.standalone.api.net.IPacket;
import com.cleanroommc.standalone.api.tileentity.StandaloneTileEntity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;

public class CPacketRecoverTE implements IPacket {

    private int dimension;
    private BlockPos pos;

    public CPacketRecoverTE() {

    }

    public CPacketRecoverTE(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buf) {
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(@Nonnull PacketBuffer buf) {
        this.dimension = buf.readVarInt();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StandaloneTileEntity) {
            ((StandaloneTileEntity) te).sendInitialSyncData();
        } else if (!(world.getBlockState(pos).getBlock() instanceof StandaloneBlock)) {
            handler.player.connection.sendPacket(new SPacketBlockChange(world, pos));
        }
    }
}
