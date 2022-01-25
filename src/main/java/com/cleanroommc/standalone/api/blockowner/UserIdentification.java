package com.cleanroommc.standalone.api.blockowner;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.UsernameCache;
import org.jline.utils.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class UserIdentification {

    private static final String NONE_MARKER = "none";
    private final UUID uuid;
    private final UUID uuid_offline;
    private final String playerName;

    @Nonnull
    public String getPlayerName() {
        if (uuid != null) {
            String lastKnownName = UsernameCache.getLastKnownUsername(uuid);
            if (lastKnownName != null)
                return lastKnownName;
        }
        return playerName;
    }

    @Nonnull
    public UUID getUUID() {
        return uuid != null ? uuid : uuid_offline;
    }

    @Nonnull
    public String getUUIDString() {
        return uuid != null ? uuid + "" : NONE_MARKER;
    }

    @Nonnull
    public GameProfile getAsGameProfile() {
        return new GameProfile(getUUID(), getPlayerName());
    }

    /**
     * Create a UserIdent from a UUID object and a name. Use this when reading stored data, it will check for username changes, implement them and write a log
     * message.
     */
    @Nonnull
    public static UserIdentification create(@Nullable UUID uuid, @Nullable String playerName) {
        if (uuid != null) {
            if (Nobody.NOBODY.equals(uuid)) {
                return Nobody.NOBODY;
            }
            if (playerName != null) {
                String lastKnownName = UsernameCache.getLastKnownUsername(uuid);
                if (lastKnownName != null && !lastKnownName.equals(playerName)) {
                    Log.warn("The user with the UUID " + uuid + " changed name from '" + playerName + "' to '" + lastKnownName + "'");
                    return new UserIdentification(uuid, lastKnownName);
                }
            }
            return new UserIdentification(uuid, playerName);
        } else if (playerName != null) {
            return new UserIdentification(null, playerName);
        }
        return Nobody.NOBODY;
    }

    /**
     * Create a UserIdent from a UUID string and a name. Use this when reading stored data, it will check for username changes, implement them and write a log
     * message.
     */
    @Nonnull
    public static UserIdentification create(@Nonnull String suuid, @Nullable String playerName) {
        if (NONE_MARKER.equals(suuid)) {
            return new UserIdentification(null, playerName);
        }
        try {
            UUID uuid = UUID.fromString(suuid);
            if (Nobody.NOBODY.equals(uuid))
                return Nobody.NOBODY;

            return create(uuid, playerName);
        } catch (IllegalArgumentException e) {
            return Nobody.NOBODY;
        }
    }

    /**
     * Create a UserIdent from a GameProfile. Use this when creating a UserIdent for a currently active player.
     */
    @Nonnull
    public static UserIdentification create(@Nullable GameProfile gameProfile) {
        if (gameProfile != null && (gameProfile.getId() != null || gameProfile.getName() != null)) {
            if (gameProfile.getId() != null && gameProfile.getName() != null && gameProfile.getId().equals(offlineUUID(gameProfile.getName())))
                return new UserIdentification(null, gameProfile.getName());
            else
                return new UserIdentification(gameProfile.getId(), gameProfile.getName());
        } else {
            return Nobody.NOBODY;
        }
    }

    @Nonnull
    private static UUID offlineUUID(@Nullable String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8));
    }

    UserIdentification(@Nullable UUID uuid, @Nullable String playerName) {
        this.uuid = uuid;
        this.uuid_offline = offlineUUID(playerName);
        this.playerName = playerName != null ? playerName : "[" + uuid + "]";
    }

    @Override
    public int hashCode() {
        return 31 * (31 + playerName.hashCode()) + ((uuid == null) ? 0 : uuid.hashCode());
    }

    /**
     * Please note that a UserIdent will successfully equal against GameProfiles and UUIDs.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof UserIdentification)
            return equals((UserIdentification) obj);
        else if (obj instanceof GameProfile)
            return equals((GameProfile) obj);
        else if (obj instanceof UUID)
            return equals((UUID) obj);

        return false;
    }

    public boolean equals(UserIdentification other) {
        if (this.uuid != null && other.uuid != null)
            return this.uuid.equals(other.uuid);

        return this.uuid_offline.equals(other.uuid_offline);
    }

    public boolean equals(@Nonnull UUID other) {
        return other.equals(uuid) || other.equals(uuid_offline);
    }

    public boolean equals(@Nonnull GameProfile other) {
        UUID other_uuid = other.getId();
        if (this.uuid != null && other_uuid != null)
            return this.uuid.equals(other_uuid);

        UUID uuid_offline_other = offlineUUID(other.getName());
        return uuid_offline_other.equals(this.uuid) || this.uuid_offline.equals(uuid_offline_other);
    }

    public void saveToNbt(@Nonnull NBTTagCompound nbt, @Nonnull String prefix) {
        if (uuid != null)
            nbt.setString(prefix + ".uuid", "" + uuid);

        nbt.setString(prefix + ".login", playerName);
    }

    public static boolean existsInNbt(@Nonnull NBTTagCompound nbt, @Nonnull String prefix) {
        return nbt.hasKey(prefix + ".uuid") || nbt.hasKey(prefix + ".login");
    }

    @Nonnull
    public static UserIdentification readfromNbt(@Nonnull NBTTagCompound nbt, @Nonnull String prefix) {
        String suuid = nbt.getString(prefix + ".uuid");
        String login = nbt.getString(prefix + ".login");

        if (Nobody.NOBODY_MARKER.equals(suuid)) {
            return Nobody.NOBODY;
        }
        try {
            UUID uuid = UUID.fromString(suuid);
            return create(uuid, login);
        } catch (IllegalArgumentException e) {
            if (login.isEmpty()) {
                return Nobody.NOBODY;
            } else {
                return new UserIdentification(null, login);
            }
        }
    }

    @Override
    public String toString() {
        return "User [uuid=" + (uuid != null ? uuid : "(unknown)") + ", name=" + playerName + "]";
    }

    public static class Nobody extends UserIdentification {

        public static final Nobody NOBODY = new Nobody();

        private static final String NOBODY_MARKER = "nobody";

        Nobody() {
            super(null, "[unknown player]");
        }

        @Override
        public boolean equals(UserIdentification other) {
            return this == other;
        }

        @Override
        public void saveToNbt(@Nonnull NBTTagCompound nbt, @Nonnull String prefix) {
            nbt.setString(prefix + ".uuid", NOBODY_MARKER);
        }

    }

}
