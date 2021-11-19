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
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity> {

    public static List<List<int[]>> offsetsByTick = getOffsetsByTick(ShockwaveEntity.LIFESPAN);
    protected static final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
    protected static final List<RenderType> renderTypes = RenderType.chunkBufferLayers();

    public ShockwaveRenderer(EntityRendererManager manager) {

        super(manager);
    }

    @Override
    public void render(ShockwaveEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {

        BlockPos origin = entity.blockPosition();
        World world = entity.level;

        for (int i = Math.max(0, entity.tickCount - ShockwaveEntity.ANIM_DURATION); i <= entity.tickCount; ++i) {
            if (i >= offsetsByTick.size()) {
                break;
            }
            List<int[]> offsets = offsetsByTick.get(i);
            for (int[] offset : offsets) {
                float ticks = entity.tickCount - i + partialTicks;
                double yOffset = 0.5 * ticks * (1.2 - 0.3 * ticks);
                for (int y = 1; y >= -1; --y) {
                    BlockPos pos = origin.offset(offset[0], y, offset[1]);
                    BlockState state = world.getBlockState(pos);
                    if (!state.isAir(world, pos) && state.isRedstoneConductor(world, pos) && state.getHarvestLevel() <= 5 &&
                            state.isCollisionShapeFullBlock(world, pos) && !state.hasTileEntity() &&
                            !world.getBlockState(pos.above()).isCollisionShapeFullBlock(world, pos.above())) {
                        if (state.getRenderShape() == BlockRenderType.MODEL) {
                            matrixStack.pushPose();
                            matrixStack.translate(-0.5 + offset[0], -0.5 + yOffset + y, -0.5 + offset[1]);
                            matrixStack.scale(1.01F, 1.01F, 1.01F);
                            for (RenderType type : renderTypes) {
                                if (RenderTypeLookup.canRenderInLayer(state, type)) {
                                    ForgeHooksClient.setRenderLayer(type);
                                    blockRenderer.getModelRenderer().renderModel(world, blockRenderer.getBlockModel(state), state, pos.relative(Direction.UP), matrixStack, buffer.getBuffer(type), false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
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

        return PlayerContainer.BLOCK_ATLAS;
    }

    protected static List<List<int[]>> getOffsetsByTick(int maxTicks) {

        List<List<int[]>> ticks = IntStream.range(0, maxTicks).mapToObj(i -> new ArrayList<int[]>()).collect(Collectors.toList());
        float max = maxTicks * ShockwaveEntity.DISTANCE_PER_TICK;
        float max2 = max * max;
        for (int x = 0; x <= MathHelper.ceil(max); ++x) {
            for (int z = 0; z <= x; ++z) {
                int distSqr = x * x + z * z;
                if (distSqr < max2) {
                    int index = Math.round(MathHelper.sqrt(distSqr) / ShockwaveEntity.DISTANCE_PER_TICK);
                    if (index < ticks.size()) {
                        addReflections(ticks.get(index), x, z);
                    }
                }
            }
        }
        return ticks;
    }

    protected static void addReflections(List<int[]> list, int x, int z) {

        list.add(new int[]{x, z});
        list.add(new int[]{-x, -z});
        if (z != 0) {
            list.add(new int[]{-x, z});
            list.add(new int[]{x, -z});
        }
        if (x != 0 && x != z) {
            list.add(new int[]{z, x});
            list.add(new int[]{-z, -x});
            if (z != 0) {
                list.add(new int[]{-z, x});
                list.add(new int[]{z, -x});
            }
        }
    }
}
