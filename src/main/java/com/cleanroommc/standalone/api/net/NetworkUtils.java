package com.cleanroommc.standalone.api.net;

import com.cleanroommc.standalone.Tags;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import javax.annotation.Nonnull;

public class NetworkUtils {

    public static void writePacketBuffer(@Nonnull PacketBuffer writeTo, @Nonnull PacketBuffer writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    @Nonnull
    public static PacketBuffer readPacketBuffer(@Nonnull PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
    }

    public static TileEntity getTileEntityServer(int dimension, BlockPos pos) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension).getTileEntity(pos);
    }

    @Nonnull
    public static IBlockState getIBlockStateServer(int dimension, BlockPos pos) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension).getBlockState(pos);
    }

    @Nonnull
    public static FMLProxyPacket packet2proxy(@Nonnull IPacket packet) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(PacketHandler.getPacketId(packet.getClass()));
        packet.encode(buf);
        return new FMLProxyPacket(buf, Tags.MODID);
    }

    @Nonnull
    public static IPacket proxy2packet(@Nonnull FMLProxyPacket proxyPacket) throws Exception {
        PacketBuffer payload = (PacketBuffer) proxyPacket.payload();
        IPacket packet = PacketHandler.getPacketClass(payload.readVarInt()).newInstance();
        packet.decode(payload);
        return packet;
    }

    @Nonnull
    public static NetworkRegistry.TargetPoint blockPoint(@Nonnull World world, @Nonnull BlockPos blockPos) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 128.0);
    }
}
