package com.cleanroommc.standalone.api.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;


/**
 * An event that can be used to respond to, edit, and prevent entity teleports.
 * <p>
 * This event will fire under all circumstances:
 * <ul>
 * <li>Travel Anchors</li>
 * <li>Staff of travelling</li>
 * <li>Telepad</li>
 * </ul>
 * <p>
 * As well as any externally added teleports, assuming they fire this event manually.
 */
@Cancelable
public class TeleportEntityEvent extends EntityEvent {

    /**
     * The target coords. These can be edited by event handlers.
     */
    private BlockPos targetPos;

    private int dimension;

    private final TravelSource source;

    /**
     * Fired before an entity teleports to the given location.
     *
     * @param entity The entity teleporting
     * @param pos    The target coord
     */
    public TeleportEntityEvent(@Nonnull Entity entity, @Nonnull TravelSource source, @Nonnull BlockPos pos, int dimension) {
        super(entity);
        this.targetPos = pos;
        this.source = source;
        this.setDimension(dimension);
    }

    @Nonnull
    public BlockPos getTarget() {
        return targetPos;
    }

    public void setTargetPos(@Nonnull BlockPos target) {
        this.targetPos = target;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    @Nonnull
    public TravelSource getSource() {
        return source;
    }
}
