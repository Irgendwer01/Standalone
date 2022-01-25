package com.cleanroommc.standalone.api.teleport;

import com.cleanroommc.standalone.client.resource.StandaloneSounds;
import com.cleanroommc.standalone.common.StandaloneConfig;
import net.minecraft.util.SoundEvent;

public enum TravelSource {

    BLOCK(StandaloneSounds.TRAVEL_SOURCE) {
        @Override
        public int getMaxDistanceTravelled() {
            return StandaloneConfig.travel.rangeBlockToBlock;
        }
    },
    STAFF(StandaloneSounds.TRAVEL_SOURCE) {
        @Override
        public int getMaxDistanceTravelled() {
            return StandaloneConfig.travel.rangeItemToBlock;
        }

        @Override
        public float getPowerCostPerBlockTraveledRF() {
            return StandaloneConfig.travel.costItemToBlock;
        }
    },
    STAFF_BLINK(StandaloneSounds.TRAVEL_SOURCE) {
        @Override
        public int getMaxDistanceTravelled() {
            return StandaloneConfig.travel.rangeItemToBlink;
        }

        @Override
        public float getPowerCostPerBlockTraveledRF() {
            return StandaloneConfig.travel.costItemToBlink;
        }
    };

    public final SoundEvent sound;

    TravelSource(SoundEvent sound) {
        this.sound = sound;
    }

    public boolean getConserveMomentum() {
        return this == STAFF_BLINK;
    }

    public int getMaxDistanceTravelled() {
        return 0;
    }

    public int getMaxDistanceTravelledSq() {
        return getMaxDistanceTravelled() * getMaxDistanceTravelled();
    }

    public float getPowerCostPerBlockTraveledRF() {
        return 0;
    }

}
