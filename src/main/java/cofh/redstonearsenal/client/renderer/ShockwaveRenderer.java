package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.entity.ShockwaveEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;
import java.util.Random;

public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity> {

    public ShockwaveRenderer(EntityRendererManager manager) {

        super(manager);
    }

    @Override
    public void render(ShockwaveEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {

        BlockPos origin = entity.blockPosition();
        World world = entity.level;
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        for (int i = Math.max(0, entity.tickCount - ShockwaveEntity.ANIM_DURATION); i <= entity.tickCount; ++i) {
            if (i >= ShockwaveEntity.offsetsByTick.size()) {
                break;
            }
            List<int[]> offsets = ShockwaveEntity.offsetsByTick.get(i);
            for (int[] offset : offsets) {
                float ticks = entity.tickCount - i + partialTicks;
                double yOffset = 0.5 * ticks * (1.2 - 0.3 * ticks);
                for (int y = 1; y >= -1; --y) {
                    BlockPos pos = origin.offset(offset[0], y, offset[1]);
                    BlockState state = world.getBlockState(pos);
                    if (!state.isAir(world, pos) && state.isRedstoneConductor(world, pos) &&
                            state.isCollisionShapeFullBlock(world, pos) && !state.hasTileEntity() &&
                            !world.getBlockState(pos.above()).isCollisionShapeFullBlock(world, pos.above())) {
                        if (state.getRenderShape() == BlockRenderType.MODEL) {
                            matrixStack.pushPose();
                            matrixStack.translate(-0.5 + offset[0], -0.5 + yOffset + y, -0.5 + offset[1]);
                            matrixStack.scale(1.01F, 1.01F, 1.01F);
                            for (RenderType type : RenderType.chunkBufferLayers()) {
                                if (RenderTypeLookup.canRenderInLayer(state, type)) {
                                    ForgeHooksClient.setRenderLayer(type);
                                    blockRenderer.getModelRenderer().renderModel(world, blockRenderer.getBlockModel(state), state, pos, matrixStack, buffer.getBuffer(type), false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                                }
                            }
                            matrixStack.popPose();
                        }
                        break;
                    }
                }
            }
        }

        ForgeHooksClient.setRenderLayer(null);
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShockwaveEntity entity) {

        return AtlasTexture.LOCATION_BLOCKS;
    }
}
