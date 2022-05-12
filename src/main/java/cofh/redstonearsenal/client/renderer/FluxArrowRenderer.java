package cofh.redstonearsenal.client.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@OnlyIn (Dist.CLIENT)
public class FluxArrowRenderer extends ArrowRenderer<AbstractArrow> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_arrow.png");

    public FluxArrowRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractArrow entity) {

        return TEXTURE;
    }

}
