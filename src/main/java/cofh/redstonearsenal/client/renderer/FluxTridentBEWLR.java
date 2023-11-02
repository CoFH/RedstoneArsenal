package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.item.FluxTridentItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class FluxTridentBEWLR extends BlockEntityWithoutLevelRenderer {

    public static final FluxTridentBEWLR INSTANCE = new FluxTridentBEWLR(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    private static final ResourceLocation UNCHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation CHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation EMPOWERED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");

    public FluxTridentBEWLR(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {

        super(dispatcher, modelSet);
    }

    //@Override
    //public void onResourceManagerReload(ResourceManager manager) {
    //
    //    this.tridentModel = new TridentModel(this.entityModelSet.bakeLayer(ModelLayers.TRIDENT));
    //}

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, int overlayCoord) {

        if (stack.getItem() instanceof FluxTridentItem) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, tridentModel.renderType(getTextureLocation(stack)), false, stack.hasFoil());
            tridentModel.renderToBuffer(matrixStack, consumer, packedLight, overlayCoord, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        } else {
            super.renderByItem(stack, displayContext, matrixStack, buffer, packedLight, overlayCoord);
        }
    }

    public ResourceLocation getTextureLocation(ItemStack stack) {

        FluxTridentItem trident = (FluxTridentItem) stack.getItem();
        if (trident.getEnergyStored(stack) > 0) {
            return trident.isEmpowered(stack) ? EMPOWERED : CHARGED;
        }
        return UNCHARGED;
    }

}
