package com.cleanroommc.standalone;

import com.cleanroommc.standalone.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;

@Mod(modid = Standalone.MODID,
        name = Standalone.NAME,
        version = Standalone.VERSION)
public class Standalone {

    public static final String MODID = "standalone";
    public static final String NAME = "Standalone";
    public static final String VERSION = "@VERSION@";

    @SidedProxy(modId = MODID, clientSide = "com.cleanroommc.standalone.proxy.ClientProxy", serverSide = "com.cleanroommc.standalone.proxy.CommonProxy")
    public static CommonProxy proxy;

}
