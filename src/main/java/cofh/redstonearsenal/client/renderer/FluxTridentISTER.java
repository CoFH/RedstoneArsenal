package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.item.FluxTridentItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.model.TridentModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
