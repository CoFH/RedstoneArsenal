package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.item.FluxTridentItem;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxTridentISTER extends ItemStackTileEntityRenderer {

    //TODO: charged/active
    private static final ResourceLocation UNCHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation CHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation EMPOWERED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private final TridentModel tridentModel = new TridentModel();

    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_239207_5_, int p_239207_6_) {

        if (stack.getItem() instanceof FluxTridentItem) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            IVertexBuilder ivertexbuilder1 = ItemRenderer.getFoilBufferDirect(buffer, tridentModel.renderType(getTextureLocation(stack)), false, stack.hasFoil());
            tridentModel.renderToBuffer(matrixStack, ivertexbuilder1, p_239207_5_, p_239207_6_, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        } else {
            super.renderByItem(stack, transformType, matrixStack, buffer, p_239207_5_, p_239207_6_);
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
