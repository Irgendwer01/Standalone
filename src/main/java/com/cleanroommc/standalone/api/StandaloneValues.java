package com.cleanroommc.standalone.api;

import com.cleanroommc.standalone.api.util.XSTR;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Random;

public class StandaloneValues {

    /**
     * Used to enable debug logging
     */
    public static final boolean DEBUG = false;

    /**
     * Fluid per Material Unit (Prime Factors: 3 * 3 * 2 * 2 * 2 * 2)
     */
    public static final int L = 144;

    /**
     * The Item WildCard Tag.
     */
    public static final short W = OreDictionary.WILDCARD_VALUE;

    public static Random RNG = new XSTR();
}
