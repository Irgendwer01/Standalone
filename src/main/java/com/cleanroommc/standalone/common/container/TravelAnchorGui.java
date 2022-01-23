package com.cleanroommc.standalone.common.container;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.GuiBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TravelAnchorGui extends GuiBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Standalone.MODID, "textures/gui/container/travel_anchor.png");

    public TravelAnchorGui(IInventory playerInv, @Nonnull IInventory inventory, EntityPlayer player) {
        super(playerInv, inventory, player, new TravelAnchorContainer(playerInv, inventory, player));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return TEXTURE;
    }
}
