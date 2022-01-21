package cofh.redstonearsenal.init;

import cofh.redstonearsenal.client.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import static cofh.redstonearsenal.init.RSAReferences.*;

public class RSAClient {

    public static void registerRenderLayers() {

        EntityRendererManager manager = Minecraft.getInstance().getEntityRenderDispatcher();
        for (PlayerRenderer renderer : manager.getSkinMap().values()) {
            renderer.addLayer(new FluxElytraLayer<>(renderer));
        }
        for (EntityRenderer<?> renderer : manager.renderers.values()) {
            if (renderer instanceof BipedRenderer || renderer instanceof ArmorStandRenderer) {
                LivingRenderer<?, ?> livingRenderer = (LivingRenderer<?, ?>) renderer;
                livingRenderer.addLayer(new FluxElytraLayer<>(livingRenderer));
            }
        }
    }

    public static void registerEntityRenderingHandlers() {

        RenderingRegistry.registerEntityRenderingHandler(FLUX_SLASH_ENTITY, FluxSlashRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_ARROW_ENTITY, FluxArrowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_TRIDENT_ENTITY, FluxTridentRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_WRENCH_ENTITY, FluxWrenchRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SHOCKWAVE_ENTITY, ShockwaveRenderer::new);
    }

}
