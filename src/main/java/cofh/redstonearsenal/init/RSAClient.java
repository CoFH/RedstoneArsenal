package cofh.redstonearsenal.init;

import cofh.redstonearsenal.client.renderer.FluxElytraLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;

public class RSAClient {

    public static void registerRenderLayers() {

        for (PlayerRenderer renderer : Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values()) {
            renderer.addLayer(new FluxElytraLayer<>(renderer));
        }
    }

}
