package com.cleanroommc.standalone.client.render;

import com.cleanroommc.standalone.api.vectors.Vector3f;
import com.cleanroommc.standalone.api.vectors.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class RenderUtil {

    public static final @Nonnull
    Vector4f DEFAULT_TEXT_SHADOW_COL = new Vector4f(0.33f, 0.33f, 0.33f, 0.33f);

    public static final @Nonnull
    Vector4f DEFAULT_TXT_COL = new Vector4f(1, 1, 1, 1);

    public static final Vector4f DEFAULT_TEXT_BG_COL = new Vector4f(0.275f, 0.08f, 0.4f, 0.75f);

    public static final ResourceLocation BLOCK_TEX = TextureMap.LOCATION_BLOCKS_TEXTURE;

    @Nonnull
    public static TextureManager engine() {
        return Minecraft.getMinecraft().renderEngine;
    }

    public static void bindBlockTexture() {
        engine().bindTexture(BLOCK_TEX);
    }

    public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size) {
        drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, DEFAULT_TEXT_BG_COL);
    }

    public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size, @Nonnull Vector4f bgCol) {
        drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, bgCol);
    }

    public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size, @Nonnull Vector4f txtCol, boolean drawShadow,
                                           @Nullable Vector4f shadowCol, boolean drawBackground, @Nullable Vector4f bgCol) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.x, pos.y, pos.z);
        GlStateManager.rotate(180, 1, 0, 0);

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fnt = mc.fontRenderer;
        float scale = size / fnt.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.translate(-fnt.getStringWidth(text) / 2.0F, 0, 0);
        if (drawBackground && bgCol != null) {
            renderBackground(fnt, text, bgCol);
        }
        fnt.drawString(text, 0, 0, ColorUtil.getRGBA(txtCol));
        if (drawShadow && shadowCol != null) {
            GlStateManager.translate(0.5f, 0.5f, 0.1f);
            fnt.drawString(text, 0, 0, ColorUtil.getRGBA(shadowCol));
        }
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();

        RenderUtil.bindBlockTexture();
    }

    public static void renderBackground(@Nonnull FontRenderer fnt, @Nonnull String toRender, @Nonnull Vector4f color) {

        GlStateManager.enableBlend(); // blend comes in as on or off depending on the player's view vector

        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL_SMOOTH);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        RenderHelper.disableStandardItemLighting();

        float width = fnt.getStringWidth(toRender);
        float height = fnt.FONT_HEIGHT;
        float padding = 2f;

        GlStateManager.color(color.x, color.y, color.z, color.w);

        BufferBuilder tes = Tessellator.getInstance().getBuffer();
        tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        tes.pos(-padding, -padding, 0).endVertex();
        tes.pos(-padding, height + padding, 0).endVertex();
        tes.pos(width + padding, height + padding, 0).endVertex();
        tes.pos(width + padding, -padding, 0).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
    }

    public static void bindTexture(@Nonnull ResourceLocation location) {
        engine().bindTexture(location);
    }
}
