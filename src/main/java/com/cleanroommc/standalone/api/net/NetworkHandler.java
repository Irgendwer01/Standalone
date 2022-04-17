package com.cleanroommc.standalone.api.net;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.net.packet.CPacketGhostSlot;
import com.cleanroommc.standalone.api.net.packet.CPacketTextFieldSync;
import com.cleanroommc.standalone.api.net.packet.CPacketTravelEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static com.cleanroommc.standalone.api.net.PacketHandler.*;

public class NetworkHandler {

    public static FMLEventChannel channel;

    private NetworkHandler() {
    }

    // Register your packets here
    public static void init() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(Standalone.MODID);
        channel.register(new NetworkHandler());

        registerPacket(CPacketTravelEvent.class);
        registerPacket(CPacketTextFieldSync.class);
        registerPacket(CPacketGhostSlot.class);

        initServer();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            initClient();
        }
    }

    // Register packets as "received on server" here
    protected static void initServer() {
        registerServerExecutor(CPacketTravelEvent.class);
        registerServerExecutor(CPacketTextFieldSync.class);
        registerServerExecutor(CPacketGhostSlot.class);
    }

    // Register packets as "received on client" here
    @SideOnly(Side.CLIENT)
    protected static void initClient() {

    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(@Nonnull FMLNetworkEvent.ClientCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (hasClientExecutor(packet.getClass())) {
            NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                packet.executeClient(handler);
            } else {
                threadListener.addScheduledTask(() -> packet.executeClient(handler));
            }
        }
    }

    @SubscribeEvent
    public void onServerPacket(@Nonnull FMLNetworkEvent.ServerCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (hasServerExecutor(packet.getClass())) {
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                packet.executeServer(handler);
            } else {
                threadListener.addScheduledTask(() -> packet.executeServer(handler));
            }
        }
    }
}
