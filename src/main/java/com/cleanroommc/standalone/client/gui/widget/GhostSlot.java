package com.cleanroommc.standalone.client.gui.widget;

import com.cleanroommc.standalone.api.GuiBase;
import com.cleanroommc.standalone.api.net.NetworkHandler;
import com.cleanroommc.standalone.api.net.packet.CPacketGhostSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public abstract class GhostSlot {

    private TileEntity tileEntity;
    private int slot = -1;
    private int x;
    private int y;
    private boolean visible = true;
    private boolean grayOut = true;
    private float grayOutLevel = 0.5F;
    private boolean displayStandardOverlay = false;
    private int stackSizeLimit = 1;
    private boolean updateServer = false;
    private boolean drawStandardTooltip = true;
    private boolean drawFakeHover = true;

    public GhostSlot() {

    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX < this.getX() + 16 && mouseY >= this.getY() && mouseY < this.getY() + 16;
    }

    @Nonnull
    public abstract ItemStack getStack();

    public void putStack(@Nonnull ItemStack stack, int size) {
        if (this.shouldUpdateServer()) {
            NetworkHandler.channel.sendToServer(new CPacketGhostSlot(getSlot(), stack, size).toFMLPacket());
        }
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean shouldGrayOut() {
        return this.grayOut;
    }

    public void setGrayOut(boolean grayOut) {
        this.grayOut = grayOut;
    }

    public float getGrayOutLevel() {
        return this.grayOutLevel;
    }

    public void setGrayOutLevel(float grayOutLevel) {
        this.grayOutLevel = grayOutLevel;
    }

    public boolean shouldDisplayStandardOverlay() {
        return this.displayStandardOverlay;
    }

    public void setDisplayStandardOverlay(boolean displayStandardOverlay) {
        this.displayStandardOverlay = displayStandardOverlay;
    }

    public int getStackSizeLimit() {
        return this.stackSizeLimit;
    }

    public void setStackSizeLimit(int stackSizeLimit) {
        this.stackSizeLimit = stackSizeLimit;
    }

    public boolean shouldDrawStandardTooltip() {
        return this.drawStandardTooltip;
    }

    public void setDrawStandardTooltip(boolean drawStandardTooltip) {
        this.drawStandardTooltip = drawStandardTooltip;
    }

    public boolean shouldUpdateServer() {
        return this.updateServer;
    }

    public void setUpdateServer(boolean updateServer) {
        this.updateServer = updateServer;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }

    public void setTileEntity(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean shouldDrawFakeHover() {
        return this.drawFakeHover;
    }

    public void setDrawFakeHover(boolean hover) {
        this.drawFakeHover = hover;
    }

    public boolean drawGhostSlotTooltip(@Nonnull GuiBase guiBase, int mouseX, int mouseY) {
        if (this.shouldDrawStandardTooltip() && guiBase.mc.player.inventory.getItemStack().isEmpty()) {
            if (!getStack().isEmpty()) {
                guiBase.renderToolTip(getStack(), mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    public interface IGhostSlotAware {
        void setGhostSlotContents(int slot, ItemStack stack, int size);
    }
}
