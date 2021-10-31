package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.item.FluxElytraItem;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    protected static final ResourceLocation UNCHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_elytra.png");
    //TODO: charged/active
    protected static final ResourceLocation CHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_elytra.png");
    protected static final ResourceLocation ACTIVE = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_elytra.png");

    public FluxElytraLayer(IEntityRenderer renderer) {

        super(renderer);
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {

        return stack.getItem() instanceof FluxElytraItem;
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {

        FluxElytraItem elytra = (FluxElytraItem) stack.getItem();
        if (elytra.getEnergyStored(stack) > 0) {
            if (elytra.getMode(stack) > 0) {
                return ACTIVE;
            }
            return CHARGED;
        }
        return UNCHARGED;
    }
}
