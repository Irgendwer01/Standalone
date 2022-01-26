package com.cleanroommc.standalone.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jline.utils.Log;

import javax.annotation.Nonnull;

public final class SoundHelper {


    public static final Vec3d BLOCK_CENTER = new Vec3d(.5, .5, .5);
    public static final Vec3d BLOCK_TOP = new Vec3d(.5, 1, .5);
    public static final Vec3d BLOCK_LOW = new Vec3d(.5, .1, .5);

    /**
     * Plays a sound at the given location. If called on a server, it will play it for all players.
     *
     */
    public static void playSound(World world, double soundLocationX, double soundLocationY, double soundLocationZ, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        if (sound != null) {
            if (world instanceof WorldServer) {
                world.playSound(null, soundLocationX, soundLocationY, soundLocationZ, sound, soundCategory, volume, pitch);
            } else {
                world.playSound(soundLocationX, soundLocationY, soundLocationZ, sound, soundCategory, volume, pitch, false);
            }
        } else {
            Log.error("SoundHelper: Asked to play invalid sound " + sound);
        }
    }

    /**
     * Plays a sound at the center of the given BlockPos. If called on a server, it will play it for all players.
     *
     */
    public static void playSound(World world, BlockPos soundLocation, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        playSound(world, soundLocation, BLOCK_CENTER, sound, soundCategory, volume, pitch);
    }

    /**
     * Plays a sound at an offset to the given BlockPos. If called on a server, it will play it for all players.
     * <p>
     *
     * See {@link SoundHelper#BLOCK_CENTER}, {@link SoundHelper#BLOCK_TOP} and {@link SoundHelper#BLOCK_LOW} for common offsets.
     *
     */
    public static void playSound(World world, @Nonnull BlockPos soundLocation, @Nonnull Vec3d offset, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        playSound(world, soundLocation.getX() + offset.x, soundLocation.getY() + offset.y, soundLocation.getZ() + offset.z, sound, soundCategory, volume, pitch);
    }

    /**
     * Plays a sound at the location of given entity. If called on a server, it will play it for all players.
     *
     */
    public static void playSound(World world, @Nonnull Entity soundLocation, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        playSound(world, soundLocation.posX, soundLocation.posY, soundLocation.posZ, sound, soundCategory, volume, pitch);
    }
}
