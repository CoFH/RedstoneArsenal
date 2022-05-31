package cofh.redstonearsenal.event;

import cofh.lib.client.renderer.entity.NothingRenderer;
import cofh.redstonearsenal.client.renderer.FluxArrowRenderer;
import cofh.redstonearsenal.client.renderer.FluxSlashRenderer;
import cofh.redstonearsenal.client.renderer.FluxWrenchRenderer;
import cofh.redstonearsenal.client.renderer.ThrownFluxTridentRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAReferences.*;

@Mod.EventBusSubscriber (value = Dist.CLIENT, modid = ID_REDSTONE_ARSENAL, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RSAClientSetupEvents {

    @SubscribeEvent
    public static void entityLayerSetup(final EntityRenderersEvent.AddLayers event) {

        EntityModelSet models = event.getEntityModels();
        //for (String skin : event.getSkins()) {
        //    LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> renderer = event.getSkin(skin);
        //    renderer.addLayer(new FluxElytraLayer<>(renderer, models));
        //}
        //
        //EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        //for (EntityRenderer<?> renderer : manager.renderers.values()) {
        //    if (renderer instanceof HumanoidMobRenderer || renderer instanceof ArmorStandRenderer) {
        //        LivingEntityRenderer<?, ?> livingRenderer = (LivingEntityRenderer<?, ?>) renderer;
        //        livingRenderer.addLayer(new FluxElytraLayer<>(livingRenderer, models));
        //    }
        //}
    }

    @SubscribeEvent
    public static void entityRendererSetup(final EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(FLUX_SLASH_ENTITY, FluxSlashRenderer::new);
        event.registerEntityRenderer(FLUX_ARROW_ENTITY, FluxArrowRenderer::new);
        event.registerEntityRenderer(FLUX_TRIDENT_ENTITY, ThrownFluxTridentRenderer::new);
        event.registerEntityRenderer(FLUX_WRENCH_ENTITY, FluxWrenchRenderer::new);
        event.registerEntityRenderer(SHOCKWAVE_ENTITY, NothingRenderer::new);
    }

}
