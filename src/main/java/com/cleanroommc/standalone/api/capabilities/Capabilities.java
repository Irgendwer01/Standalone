package com.cleanroommc.standalone.api.capabilities;

import gregtech.api.capability.IElectricItem;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Capabilities added by other mods like GTCE(u).
 */
public class Capabilities {

    public static Capability<IElectricItem> GTCE_ENERGY_ITEM;

    @CapabilityInject(IElectricItem.class)
    private static void capIEnergyContainer(Capability<IElectricItem> cap) {
            GTCE_ENERGY_ITEM = cap;
    }
}
