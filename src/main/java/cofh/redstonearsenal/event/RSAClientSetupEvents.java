package cofh.redstonearsenal.event;

import cofh.lib.client.renderer.entity.NothingRenderer;
import cofh.redstonearsenal.client.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAReferences.*;

@Mod.EventBusSubscriber (value = Dist.CLIENT, modid = ID_REDSTONE_ARSENAL, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RSAClientSetupEvents {

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void entityLayerSetup(final EntityRenderersEvent.AddLayers event) {

        //EntityModelSet models = event.getEntityModels();
        //for (String skin : event.getSkins()) {
        //    LivingEntityRenderer<T, M> renderer = event.getSkin(skin);
        //    renderer.addLayer(new FluxElytraLayer<T extends Player, ? extends EntityModel<? extends Player>>(renderer, models));
        //}

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
        event.registerEntityRenderer(FISH_HOOK_ENTITY, FluxFishingHookRenderer::new);
    }

}
