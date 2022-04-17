package com.cleanroommc.standalone.api.teleport;

import com.cleanroommc.standalone.Standalone;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jline.utils.Log;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * A short-term chunkloading manager meant to keep both source and target chunk of a long-range player teleport chunkloaded for 5 seconds. This could help
 * prevent the server from losing track of the player.
 */

@Mod.EventBusSubscriber(modid = Standalone.MODID)
public class ChunkTicket {

    private final ForgeChunkManager.Ticket ticket;
    private long discardTime;

    private ChunkTicket(@Nonnull ForgeChunkManager.Ticket ticket) {
        this.ticket = ticket;
        this.discardTime = Standalone.proxy.getServerTickCount() + 5 * 20;
    }

    private boolean isForPlayer(@Nonnull EntityPlayerMP player, @Nonnull World world) {
        if (ticket.getPlayerName().equals(player.getUniqueID().toString()) && ticket.world == world) {
            discardTime = Standalone.proxy.getServerTickCount() + 5 * 20;
            return true;
        }
        return false;
    }

    private boolean shallDiscard() {
        if (discardTime < Standalone.proxy.getServerTickCount()) {
            Log.debug("Discarding ticket for ", ticket.getPlayerName());
            ForgeChunkManager.releaseTicket(ticket);
            return true;
        }
        return false;
    }

    private static final List<ChunkTicket> TICKETS = NonNullList.create();

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        TICKETS.clear();
    }

    @SubscribeEvent
    public static void onServerTick(@Nonnull TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !TICKETS.isEmpty()) {
            TICKETS.removeIf(ChunkTicket::shallDiscard);
        }
    }

    public static void loadChunk(@Nonnull EntityPlayerMP player, @Nonnull World world, @Nonnull BlockPos pos) {
        String playerID = player.getUniqueID().toString();
        ChunkTicket discard = null;
        if (!TICKETS.isEmpty()) {
            for (Iterator<ChunkTicket> i = TICKETS.iterator(); i.hasNext() && discard == null; ) {
                discard = i.next();
                if (!discard.isForPlayer(player, world)) {
                    discard = null;
                }
            }
        }
        if (discard == null) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestPlayerTicket(Standalone.INSTANCE, playerID, world, ForgeChunkManager.Type.NORMAL);
            if (ticket != null) {
                ticket.setChunkListDepth(2);
                TICKETS.add(discard = new ChunkTicket(ticket));
            }
        }
        if (discard != null) {
            ForgeChunkManager.forceChunk(discard.ticket, new ChunkPos(pos));
            Log.debug("Forcing chunk ", new ChunkPos(pos), " for " + discard.ticket.getPlayerName());
        }
    }
}
