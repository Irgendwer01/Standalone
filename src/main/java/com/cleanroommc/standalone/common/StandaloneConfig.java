package com.cleanroommc.standalone.common;

import com.cleanroommc.standalone.Standalone;
import net.minecraftforge.common.config.Config;

@Config(modid = Standalone.MODID)
public class StandaloneConfig {

    public static TravelOptions travel = new TravelOptions();

    public static class TravelOptions {

        @Config.Comment({"Maximum range in blocks between two blocks (Anchor -> Anchor).", "Default: 96"})
        @Config.RangeInt(min = 16, max = 16 * 32)
        public int rangeBlockToBlock = 96;

        @Config.Comment({"Maximum range in blocks when travelling with an item to a block (Staff -> Anchor).", "Default: 256"})
        @Config.RangeInt(min = 16, max = 16 * 32)
        public int rangeItemToBlock = 256;

        @Config.Comment({"Energy cost per block traveled for an item to a block. (Staff -> Anchor).", "Default: 250"})
        @Config.RangeInt(min = 0, max = 999_999)
        public int costItemToBlock = 250;

        @Config.Comment({"Maximum range in blocks when blinking with an item.", "Default: 16"})
        @Config.RangeInt(min = 4, max = 16 * 32)
        public int rangeItemToBlink = 16;

        @Config.Comment({"Energy cost per block traveled for an item blink..", "Default: 250"})
        @Config.RangeInt(min = 0, max = 999_999)
        public int costItemToBlink = 250;
    }
}
