package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;

import static cofh.redstonearsenal.RedstoneArsenal.ENTITIES;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSAEntities {

    private RSAEntities() {

    }

    public static void register() {

    }

    public static final RegistryObject<EntityType<FluxSlash>> FLUX_SLASH = ENTITIES.register(ID_FLUX_SLASH, () -> EntityType.Builder.<FluxSlash>of(FluxSlash::new, MobCategory.MISC).sized(3.0F, 0.5F).fireImmune().build(ID_FLUX_SLASH));
    public static final RegistryObject<EntityType<FluxArrow>> FLUX_ARROW = ENTITIES.register(ID_FLUX_ARROW, () -> EntityType.Builder.<FluxArrow>of(FluxArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune().build(ID_FLUX_ARROW));
    public static final RegistryObject<EntityType<ThrownFluxTrident>> FLUX_TRIDENT = ENTITIES.register(ID_FLUX_TRIDENT, () -> EntityType.Builder.<ThrownFluxTrident>of(ThrownFluxTrident::new, MobCategory.MISC).sized(0.5F, 0.5F).build(ID_FLUX_TRIDENT));
    public static final RegistryObject<EntityType<ThrownFluxWrench>> FLUX_WRENCH = ENTITIES.register(ID_FLUX_WRENCH, () -> EntityType.Builder.<ThrownFluxWrench>of(ThrownFluxWrench::new, MobCategory.MISC).sized(0.75F, 0.5F).build(ID_FLUX_WRENCH));
    public static final RegistryObject<EntityType<Shockwave>> SHOCKWAVE_ENTITY = ENTITIES.register(ID_SHOCKWAVE, () -> EntityType.Builder.<Shockwave>of(Shockwave::new, MobCategory.MISC).sized(0.1F, 0.1F).fireImmune().build(ID_SHOCKWAVE));
    public static final RegistryObject<EntityType<FluxFishingHook>> FISH_HOOK = ENTITIES.register(ID_FISH_HOOK, () -> EntityType.Builder.<FluxFishingHook>of(FluxFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build(ID_FISH_HOOK));

}
