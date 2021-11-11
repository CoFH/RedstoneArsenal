package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.FluxSlashEntity;
import cofh.redstonearsenal.entity.FluxTridentEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import static cofh.redstonearsenal.RedstoneArsenal.ENTITIES;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_SLASH;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_TRIDENT;

public class RSAEntities {

    private RSAEntities() {

    }

    public static void register() {

        ENTITIES.register(ID_FLUX_SLASH, () -> EntityType.Builder.<FluxSlashEntity>of(FluxSlashEntity::new, EntityClassification.MISC).sized(3.0F, 0.5F).build(ID_FLUX_SLASH));
        ENTITIES.register(ID_FLUX_TRIDENT, () -> EntityType.Builder.<FluxTridentEntity>of(FluxTridentEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(ID_FLUX_TRIDENT));
    }
}
