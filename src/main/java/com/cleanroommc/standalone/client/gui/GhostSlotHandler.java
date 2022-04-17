package com.cleanroommc.standalone.client.gui;

import com.cleanroommc.standalone.api.GuiBase;
import com.cleanroommc.standalone.client.gui.widget.GhostSlot;
import com.cleanroommc.standalone.client.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GhostSlotHandler {

    protected final NonNullList<GhostSlot> ghostSlots = NonNullList.create();
    protected GhostSlot hoverGhostSlot;

    public GhostSlotHandler() {

    }

    @Nonnull
    public NonNullList<GhostSlot> getGhostSlots() {
        return this.ghostSlots;
    }

    public void add(GhostSlot slot) {
        ghostSlots.add(slot);
    }

    @Nullable
    public GhostSlot getGhostSlotAt(@Nonnull GuiBase guiBase, int mouseX, int mouseY) {
        int mX = mouseX - guiBase.getGuiLeft();
        int mY = mouseY - guiBase.getGuiTop();
        for (GhostSlot slot : ghostSlots) {
            if (slot.isVisible() && slot.isMouseOver(mX, mY) && slot.shouldDrawFakeHover()) {
                return slot;
            }
        }
        return null;
    }

    public void ghostSlotClicked(@Nonnull GuiBase guiBase, @Nonnull GhostSlot slot, int x, int y, int button) {
        ItemStack handStack = Minecraft.getMinecraft().player.inventory.getItemStack();
        ItemStack existingStack = slot.getStack();
        if (button == 0) { // left
            ghostSlotClickedPrimaryMouseButton(slot, handStack, existingStack);
        } else if (button == 1) { // right
            ghostSlotClickedSecondaryMouseButton(slot, handStack, existingStack);
        } else if (button == -2) { // wheel up
            ghostSlotClickedMouseWheelUp(slot, handStack, existingStack);
        } else if (button == -1) { // wheel down
            ghostSlotClickedMouseWheelDown(slot, handStack, existingStack);
        }
    }

    protected void ghostSlotClickedPrimaryMouseButton(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
        if (handStack.isEmpty()) { // empty hand
            slot.putStack(ItemStack.EMPTY, 0);
        } else { // item in hand
            if (existingStack.isEmpty()) { // empty slot
                replaceSlot(slot, handStack);
            } else { // filled slot
                if (existingStack.isItemEqual(handStack)) { // same item
                    if (existingStack.getCount() < existingStack.getMaxStackSize() && existingStack.getCount() < slot.getStackSizeLimit()) {
                        increaseSlot(slot, existingStack);
                    }
                } else { // different item
                    replaceSlot(slot, handStack);
                }
            }
        }
    }

    protected void ghostSlotClickedSecondaryMouseButton(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
        if (handStack.isEmpty()) { // empty hand
            slot.putStack(ItemStack.EMPTY, 0);
        } else { // item in hand
            if (existingStack.isEmpty()) { // empty slot
                replaceSlot1Item(slot, handStack);
            } else { // filled slot
                if (existingStack.isItemEqual(handStack)) { // same item
                    decreaseSlot(slot, existingStack);
                } else { // different item
                    replaceSlot1Item(slot, handStack);
                }
            }
        }
    }

    protected void ghostSlotClickedMouseWheelUp(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
        if (!existingStack.isEmpty() && existingStack.getCount() < existingStack.getMaxStackSize() && existingStack.getCount() < slot.getStackSizeLimit()) {
            increaseSlot(slot, existingStack);
        }
    }

    protected void ghostSlotClickedMouseWheelDown(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack, @Nonnull ItemStack existingStack) {
        if (!existingStack.isEmpty()) {
            decreaseSlot(slot, existingStack);
        }
    }

    protected void decreaseSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack existingStack) {
        existingStack.shrink(1);
        slot.putStack(existingStack, existingStack.getCount());
    }

    protected void increaseSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack existingStack) {
        existingStack.grow(1);
        slot.putStack(existingStack, existingStack.getCount());
    }

    protected void replaceSlot1Item(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack) {
        ItemStack oneItem = handStack.copy();
        oneItem.setCount(1);
        slot.putStack(oneItem, oneItem.getCount());
    }

    protected void replaceSlot(@Nonnull GhostSlot slot, @Nonnull ItemStack handStack) {
        if (handStack.getCount() <= slot.getStackSizeLimit()) {
            slot.putStack(handStack, handStack.getCount());
        } else {
            ItemStack tmp = handStack.copy();
            tmp.setCount(slot.getStackSizeLimit());
            slot.putStack(tmp, tmp.getCount());
        }
    }

    protected void startDrawing(@Nonnull GuiBase gui) {
        hoverGhostSlot = null;
    }

    public void drawGhostSlots(@Nonnull GuiBase gui, int mouseX, int mouseY) {
        int sx = gui.getGuiLeft();
        int sy = gui.getGuiTop();
        gui.drawFakeItemsStart();
        try {
            hoverGhostSlot = null;
            for (GhostSlot slot : ghostSlots) {
                ItemStack stack = slot.getStack();
                if (slot.isVisible()) {
                    if (!stack.isEmpty()) {
                        stack = stack.copy();
                        gui.drawFakeItemStack(slot.getX() + sx, slot.getY() + sy, stack);
                        if (slot.shouldDisplayStandardOverlay()) {
                            gui.drawFakeItemStackStdOverlay(slot.getX() + sx, slot.getY() + sy, stack);
                        }
                        if (slot.shouldGrayOut()) {
                            drawGhostSlotGrayout(gui, slot);
                        }
                    }
                    if (slot.isMouseOver(mouseX - sx, mouseY - sy)) {
                        hoverGhostSlot = slot;
                    }
                }
            }
            if (hoverGhostSlot != null && hoverGhostSlot.shouldDrawFakeHover()) {
                // draw hover last to prevent it from affecting rendering of other slots...
                gui.drawFakeItemHover(hoverGhostSlot.getX() + sx, hoverGhostSlot.getY() + sy);
            }
        } finally {
            gui.drawFakeItemsEnd();
        }
    }

    /**
     * Gray out the item that was just painted into a GhostSlot by over-painting it with 50% transparent background. This gives the illusion that the item was
     * painted with 50% transparency. (100%*a ° 100%*b ° 50%*a == 100%*a ° 50%*b)
     */
    protected void drawGhostSlotGrayout(@Nonnull GuiBase gui, @Nonnull GhostSlot slot) {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, slot.getGrayOutLevel());
        RenderUtil.bindTexture(gui.getGuiTexture());
        gui.drawTexturedModalRect(gui.getGuiLeft() + slot.getX(), gui.getGuiTop() + slot.getY(), slot.getX(), slot.getY(), 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    protected boolean drawGhostSlotTooltip(@Nonnull GuiBase gui, int mouseX, int mouseY) {
        if (hoverGhostSlot != null) {
            return hoverGhostSlot.drawGhostSlotTooltip(gui, mouseX, mouseY);
        }
        return false;
    }
}
