package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.FluxArrowEntity;
import cofh.redstonearsenal.entity.FluxSlashEntity;
import cofh.redstonearsenal.entity.FluxTridentEntity;
import cofh.redstonearsenal.entity.ShockwaveEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import static cofh.redstonearsenal.RedstoneArsenal.ENTITIES;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSAEntities {

    private RSAEntities() {

    }

    public static void register() {

        ENTITIES.register(ID_FLUX_SLASH, () -> EntityType.Builder.<FluxSlashEntity>of(FluxSlashEntity::new, EntityClassification.MISC).sized(3.0F, 0.5F).fireImmune().build(ID_FLUX_SLASH));
        ENTITIES.register(ID_FLUX_ARROW, () -> EntityType.Builder.<FluxArrowEntity>of(FluxArrowEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).fireImmune().build(ID_FLUX_ARROW));
        ENTITIES.register(ID_FLUX_TRIDENT, () -> EntityType.Builder.<FluxTridentEntity>of(FluxTridentEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(ID_FLUX_TRIDENT));
        ENTITIES.register(ID_SHOCKWAVE, () -> EntityType.Builder.<ShockwaveEntity>of(ShockwaveEntity::new, EntityClassification.MISC).sized(0.1F, 0.1F).build(ID_SHOCKWAVE));

    }
}
