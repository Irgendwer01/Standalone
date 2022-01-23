package com.cleanroommc.standalone.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public abstract class GuiBase extends GuiContainer {

    private final IInventory playerInv;
    private final IInventory inventory;

    public GuiBase(IInventory playerInv, IInventory inventory, EntityPlayer player, Container container) {
        super(container);

        this.playerInv = playerInv;
        this.inventory = inventory;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = inventory.getDisplayName().getUnformattedText();
        fontRenderer.drawString(s, 8, 6, 0x404040);
        fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(getGuiTexture());
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
    }

    protected abstract ResourceLocation getGuiTexture();
}
