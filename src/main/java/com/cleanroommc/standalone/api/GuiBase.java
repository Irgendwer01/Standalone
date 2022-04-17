package com.cleanroommc.standalone.api;

import com.cleanroommc.standalone.client.gui.GhostSlotHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class GuiBase extends GuiContainer {

    private final IInventory playerInv;
    private final TileEntity tileEntity;
    protected GhostSlotHandler ghostSlotHandler = new GhostSlotHandler();

    public GuiBase(IInventory playerInv, TileEntity tileEntity, EntityPlayer player, Container container) {
        super(container);
        this.playerInv = playerInv;
        this.tileEntity = tileEntity;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (tileEntity.getDisplayName() != null) {
            fontRenderer.drawString(tileEntity.getDisplayName().getUnformattedText(), getInventoryNameX(), getInventoryNameY(), getGuiLabelColor());
        }
    }

    protected int getGuiLabelColor() {
        return 0x404040;
    }

    protected int getInventoryNameX() {
        return 8;
    }

    protected int getInventoryNameY() {
        return 6;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(getGuiTexture());
        drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
            ghostSlotHandler.drawGhostSlots(this, mouseX, mouseY);
        }
    }

    public abstract ResourceLocation getGuiTexture();

    // make public
    public void renderToolTip(@Nonnull ItemStack stack, int mouseX, int mouseY) {
        super.renderToolTip(stack, mouseX, mouseY);
    }

    public void drawFakeItemsStart() {
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
    }

    protected TileEntity getTileEntity() {
        return this.tileEntity;
    }

    public void drawFakeItemStack(int x, int y, @Nonnull ItemStack stack) {
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        GlStateManager.enableAlpha();
    }

    public void drawFakeItemStackStdOverlay(int x, int y, @Nonnull ItemStack stack) {
        itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, null);
    }

    public void drawFakeItemHover(int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    public void drawFakeItemsEnd() {
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }
}
