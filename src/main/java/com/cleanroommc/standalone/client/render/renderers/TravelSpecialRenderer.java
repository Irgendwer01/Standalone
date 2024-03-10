package com.cleanroommc.standalone.client.render.renderers;

import com.cleanroommc.standalone.api.teleport.TravelController;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.api.util.BlockCoord;
import com.cleanroommc.standalone.api.util.StandaloneUtilities;
import com.cleanroommc.standalone.api.vectors.Vector3d;
import com.cleanroommc.standalone.api.vectors.Vector3f;
import com.cleanroommc.standalone.api.vectors.Vector4f;
import com.cleanroommc.standalone.client.render.RenderUtil;
import com.cleanroommc.standalone.common.blocks.StandaloneBlocks;
import com.cleanroommc.standalone.common.tileentity.travelanchor.TileEntityTravelAnchor;
import com.cleanroommc.standalone.utils.StandaloneLog;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class TravelSpecialRenderer<T extends TileEntityTravelAnchor> extends ManagedTESR<T> {

    private final Vector4f selectedColor = new Vector4f(1, 0.25f, 0, 0.5f);
    private final Vector4f itemBlend = new Vector4f(0.3f, 0.3f, 0.3f, 0.3f);
    private final Vector4f blockBlend = new Vector4f(0.6f, 0.6f, 0.6f, 0.4f);
    private final Vector4f selectedBlockBlend = new Vector4f(0.9f, 0.33f, 0.1f, 0.35f);

    public TravelSpecialRenderer(Block block) {
        super(block);
    }

    public TravelSpecialRenderer() {
        super(StandaloneBlocks.TRAVEL_ANCHOR);
    }

    @Override
    public boolean shouldRender(@Nonnull T te, @Nonnull IBlockState blockState, int renderPass) {
        return TravelController.showTargets() && te.isVisible()
                && (TravelController.getPosPlayerOn() == null || BlockCoord.getDist(TravelController.getPosPlayerOn(), te.getLocation()) > 2);
    }

    @Override
    public void renderTileEntity(@Nonnull T te, @Nonnull IBlockState blockState, float partialTicks, int destroyStage) {
        Vector3d eye = StandaloneUtilities.getEyePositionStandalone(Minecraft.getMinecraft().player);
        Vector3d loc = new Vector3d(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5);
        int maxDistance = TravelController.isTravelItemActiveForRendering(Minecraft.getMinecraft().player) ? TravelSource.STAFF.getMaxDistanceTravelledSq()
                : TravelSource.BLOCK.getMaxDistanceTravelledSq();
        if (eye.distanceSquared(loc) > maxDistance) {
            return;
        }

        double sf = TravelController.getScaleForCandidate(loc, maxDistance);
        boolean highlight = TravelController.isBlockSelected(te.getLocation());

        TravelController.addCandidate(te.getLocation());

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.enableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        if (te.getItemLabel().isEmpty() || te.getItemLabel() == null)
        renderBlock(te.getPos(), te.getWorld(), sf, highlight);
        renderItemLabel(te.getItemLabel(), sf);
        renderLabel(te.getLabel(), sf, highlight);

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableDepth();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
    }

    private void renderItemLabel(@Nonnull ItemStack itemLabel, double globalScale) {
        if (!itemLabel.isEmpty()) {
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5f, 0.75f, 0.5f);

            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(globalScale, globalScale, globalScale);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();

            IBakedModel bakedmodel = itemRenderer.getItemModelWithOverrides(itemLabel, (World) null, (EntityLivingBase) null);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR, GlStateManager.SourceFactor.ONE.factor,
                    GlStateManager.DestFactor.ZERO.factor);
            GL14.glBlendColor(itemBlend.x, itemBlend.y, itemBlend.z, itemBlend.w);
            bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
            if (bakedmodel == null) {
                StandaloneLog.logger.warn("handleCameraTransforms returned null!");
                return;
            }
            itemRenderer.renderItem(itemLabel, bakedmodel);

            RenderHelper.disableStandardItemLighting();

            GL14.glBlendColor(1, 1, 1, 1);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.popMatrix();
        }
    }

    private void renderLabel(String toRender, double globalScale, boolean highlight) {
        if (toRender != null && toRender.trim().length() > 0) {
            GlStateManager.color(1, 1, 1, 1);
            Vector4f bgCol = RenderUtil.DEFAULT_TEXT_BG_COL;
            if (highlight) {
                bgCol = new Vector4f(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5f, 0.5f, 0.5f);
            GlStateManager.scale(globalScale, globalScale, globalScale);
            Vector3f pos = new Vector3f(0, 1.2f, 0);
            float size = 0.5f;
            RenderUtil.drawBillboardedText(pos, toRender, size, bgCol);
            GL11.glPopMatrix();
        }
    }

    public void renderBlock(@Nonnull BlockPos pos, @Nonnull IBlockAccess blockAccess, double globalScale, boolean highlight) {
        BufferBuilder tes = Tessellator.getInstance().getBuffer();
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f, 0.5f);
        GlStateManager.scale(globalScale, globalScale, globalScale);
        GlStateManager.translate(-0.5f, -0.5f, -0.5f);
        IBlockState state = blockAccess.getBlockState(pos).getActualState(blockAccess, pos);
        IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);
        state = state.getBlock().getExtendedState(state, blockAccess, pos);

        tes.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
        Vector4f color = highlight ? selectedBlockBlend : blockBlend;

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR, GlStateManager.SourceFactor.ONE.factor,
                GlStateManager.DestFactor.ZERO.factor);
        GL14.glBlendColor(color.x, color.y, color.z, color.w);

        tes.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            if (layer == null) {
                StandaloneLog.logger.warn("BlockRenderLayer value was null!");
                continue;
            }
            if (state.getBlock().canRenderInLayer(state, layer)) {
                ForgeHooksClient.setRenderLayer(layer);
                blockrendererdispatcher.getBlockModelRenderer().renderModel(blockAccess, ibakedmodel, state, pos, tes, false);
            }
        }
        ForgeHooksClient.setRenderLayer(null);
        Tessellator.getInstance().draw();

        GL14.glBlendColor(1, 1, 1, 1);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        tes.setTranslation(0, 0, 0);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull T te) {
        return true;
    }

}

