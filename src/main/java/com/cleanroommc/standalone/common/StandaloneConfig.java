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

        @Config.Comment({"Energy cost per block traveled for an item blink.", "Default: 250"})
        @Config.RangeInt(min = 0, max = 999_999)
        public int costItemToBlink = 250;

        @Config.Comment({"Whether the player can direct travel between blocks (Anchor -> Anchor) by jumping.", "At least one of allowSneaking/allowJumping must be enabled.", "Default: true"})
        public boolean allowJumping = true;

        @Config.Comment({"Whether the player can direct travel between blocks (Anchor -> Anchor) by sneaking.", "At least one of allowSneaking/allowJumping must be enabled.", "Default: true"})
        public boolean allowSneaking = true;

        @Config.Comment({"Visual size of possible targets when travelling to blocks.", "Default: 0.2"})
        @Config.RangeDouble(min = 0.01f, max = 1f)
        public float visualScale = 0.2f;

        @Config.Comment({"Whether to allow item blinking (teleport without target).", "Default: true"})
        public boolean enableBlink = true;

        @Config.Comment({"Whether to allow item blinking through solid blocks.", "Default: true"})
        public boolean enableBlinkSolidBlocks = true;

        @Config.Comment({"Whether to allow item blinking through non-solid (transparent/partial) blocks.", "Default: true"})
        public boolean enableBlinkNonSolidBlocks = true;

        @Config.Comment({"Whether to allow item blinking through unbreakable (e.g. bedrock) blocks.", "Default: false"})
        public boolean enableBlinkUnbreakableBlocks = false;

        @Config.Comment({"Blocks that cannot be blinked through.", "Default: Thaumcraft:blockWarded"})
        public String[] blockBlacklist = {
                "Thaumcraft:blockWarded"
        };

        @Config.Comment({"Minimum number of ticks between blinks. Values of 10 or less allow a limited sort of flight.", "Default: 10"})
        @Config.RangeInt(min = 0)
        public int blinkDelay =  10;

        @Config.Comment({"Whether to allow using blink when in the offhand.", "Default: true"})
        public boolean enableOffHandBlink = true;

        @Config.Comment({"Whether to allow travelling to blocks (Staff -> Anchor) when in the offhand.", "Default: true"})
        public boolean enableOffHandTravel = true;

        @Config.Comment({"Whether to create particles when travelling.", "Default: true"})
        public boolean createParticles = true;
    }
}
