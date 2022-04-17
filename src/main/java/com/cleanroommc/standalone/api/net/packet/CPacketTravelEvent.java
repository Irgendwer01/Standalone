package com.cleanroommc.standalone.api.net.packet;

import com.cleanroommc.standalone.api.net.IPacket;
import com.cleanroommc.standalone.api.teleport.ITravelItem;
import com.cleanroommc.standalone.api.teleport.TeleportEntityEvent;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.api.util.StandaloneUtilities;
import com.cleanroommc.standalone.api.vectors.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public class CPacketTravelEvent implements IPacket {

    private BlockPos pos;
    private int powerUse;
    private boolean conserveMotion;
    private int source;
    private int hand;

    public CPacketTravelEvent() {

    }

    public CPacketTravelEvent(@Nonnull BlockPos pos, int powerUse, boolean conserveMotion, @Nonnull TravelSource source, EnumHand hand) {
        this.pos = pos;
        this.powerUse = powerUse;
        this.conserveMotion = conserveMotion;
        this.source = source.ordinal();
        this.hand = (hand == null ? EnumHand.MAIN_HAND : hand).ordinal();
    }

    @Override
    public void encode(@Nonnull PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(powerUse);
        buf.writeBoolean(conserveMotion);
        buf.writeInt(source);
        buf.writeInt(hand);
    }

    @Override
    public void decode(@Nonnull PacketBuffer buf) {
        pos = buf.readBlockPos();
        powerUse = buf.readInt();
        conserveMotion = buf.readBoolean();
        source = buf.readInt();
        hand = buf.readInt();
    }

    @Override
    public void executeServer(@Nonnull NetHandlerPlayServer handler) {
        doServerTeleport(handler.player, pos, powerUse, conserveMotion, TravelSource.values()[source], EnumHand.values()[hand]);
    }

    private void doServerTeleport(@Nonnull Entity toTp, @Nonnull BlockPos pos, int powerUse, boolean conserveMotion, @Nonnull TravelSource source, @Nonnull EnumHand hand) {
        EntityPlayer player = toTp instanceof EntityPlayer ? (EntityPlayer) toTp : null;

        TeleportEntityEvent evt = new TeleportEntityEvent(toTp, source, pos, toTp.dimension);
        if (MinecraftForge.EVENT_BUS.post(evt))
            return;

        pos = evt.getTarget();

        if (player != null) {
            player.world.playSound(null, player.getPosition(), source.sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
            player.world.playSound(null, player.getPosition(), source.sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            toTp.setPosition(pos.getX(), pos.getY(), pos.getZ());
        }

        toTp.fallDistance = 0;

        if (player != null) {
            if (conserveMotion) {
                Vector3d velocityVex = StandaloneUtilities.getLookVecStandalone(player);
                SPacketEntityVelocity p = new SPacketEntityVelocity(toTp.getEntityId(), velocityVex.x, velocityVex.y, velocityVex.z);
                ((EntityPlayerMP) player).connection.sendPacket(p);
            }

            if (powerUse > 0 && !player.isCreative()) {
                ItemStack heldItem = player.getHeldItem(hand);
                if (heldItem.getItem() instanceof ITravelItem) {
                    ItemStack item = heldItem.copy();
                    ((ITravelItem) item.getItem()).extractInternal(item, powerUse);
                    player.setHeldItem(hand, item);
                }
            }
        }
    }
}
