package com.cleanroommc.standalone.api.tileentity;

import com.cleanroommc.standalone.api.StandaloneValues;
import net.minecraft.util.ITickable;

public abstract class TickableTileEntityBase extends SyncedTileEntityBase implements ITickable {

    private long timer = 0L;

    // Create an offset [0,20) to distribute ticks more evenly
    private final int offset = StandaloneValues.RNG.nextInt(20);

    public boolean isFirstTick() {
        return timer == 0;
    }

    /**
     * @return Timer value with a random offset of [0,20].
     */
    public long getOffsetTimer() {
        return timer + offset;
    }

    @Override
    public void update() {
        if (timer == 0) {
            onFirstTick();
        }
        timer++;
    }

    protected abstract void onFirstTick();
}
