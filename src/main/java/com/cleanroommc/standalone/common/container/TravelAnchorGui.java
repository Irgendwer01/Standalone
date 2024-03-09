package com.cleanroommc.standalone.common.container;

import com.cleanroommc.standalone.Tags;
import com.cleanroommc.standalone.api.GuiBase;
import com.cleanroommc.standalone.api.net.NetworkHandler;
import com.cleanroommc.standalone.api.net.packet.CPacketTextFieldSync;
import com.cleanroommc.standalone.api.teleport.ITravelAccessible;
import com.cleanroommc.standalone.client.gui.widget.GhostSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TravelAnchorGui extends GuiBase implements IContainerListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/container/travel_anchor.png");

    private GuiTextField nameField;

    public TravelAnchorGui(IInventory playerInv, @Nonnull TileEntity tileEntity, EntityPlayer player) {
        super(playerInv, tileEntity, player, new TravelAnchorContainer(playerInv, (IInventory) tileEntity, player));
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return TEXTURE;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.nameField = new GuiTextField(0, this.fontRenderer, guiLeft + 30, guiTop + 24, 80, 17);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(35);

        String label = ((ITravelAccessible) getTileEntity()).getLabel();
        this.nameField.setText(label);

        ghostSlotHandler.add(new TravelAnchorContainer.TravelAnchorGhostSlot((ITravelAccessible) getTileEntity(), 0, 134, 20));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            updateCustomName();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void updateCustomName() {
        // update the name on the client
        ((ITravelAccessible) getTileEntity()).setLabel(this.nameField.getText());
        // update the name on the server
        NetworkHandler.channel.sendToServer(new CPacketTextFieldSync(nameField.getText(), getTileEntity().getPos(), 35).toFMLPacket());
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
            GhostSlot slot = ghostSlotHandler.getGhostSlotAt(this, mouseX, mouseY);
            if (slot != null) {
                ghostSlotHandler.ghostSlotClicked(this, slot, mouseX, mouseY, mouseButton);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.nameField.drawTextBox();
        this.ghostSlotHandler.drawGhostSlots(this, mouseX, mouseY);
    }

    @Override
    public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    @Override
    public void sendSlotContents(@Nonnull Container containerToSend, int slotInd, @Nonnull ItemStack stack) {
        this.nameField.setEnabled(true);
        String label = ((ITravelAccessible) getTileEntity()).getLabel();
        this.nameField.setText(label);

        if (!this.nameField.getText().isEmpty()) {
            this.updateCustomName();
        }
    }

    public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {

    }

    public void sendAllWindowProperties(@Nonnull Container containerIn, @Nonnull IInventory inventory) {

    }
}
