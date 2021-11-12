package cofh.redstonearsenal.client.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxArrowRenderer<T> extends ArrowRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_arrow.png");

    public FluxArrowRenderer(EntityRendererManager manager) {

        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {

        return TEXTURE;
    }
}
