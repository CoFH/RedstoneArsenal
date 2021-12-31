package cofh.redstonearsenal.init;

import cofh.redstonearsenal.client.renderer.FluxElytraLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;

public class RSAClient {

    public static void registerRenderLayers() {

        EntityRendererManager manager = Minecraft.getInstance().getEntityRenderDispatcher();
        for (PlayerRenderer renderer : manager.getSkinMap().values()) {
            renderer.addLayer(new FluxElytraLayer<>(renderer));
        }
        for (EntityRenderer<?> renderer : manager.renderers.values()) {
            if (renderer instanceof BipedRenderer || renderer instanceof ArmorStandRenderer) {
                LivingRenderer<?,?> livingRenderer = (LivingRenderer<?, ?>) renderer;
                livingRenderer.addLayer(new FluxElytraLayer<>(livingRenderer));
            }
        }
    }

}
