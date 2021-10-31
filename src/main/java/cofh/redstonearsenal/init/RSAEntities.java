package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.FluxSlashEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import static cofh.redstonearsenal.RedstoneArsenal.ENTITIES;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSAEntities {

    private RSAEntities() {

    }

    public static void register() {

        ENTITIES.register(ID_SWORD_PROJECTILE, () -> EntityType.Builder.<FluxSlashEntity>of(FluxSlashEntity::new, EntityClassification.MISC).sized(3.0F, 0.5F).build(ID_SWORD_PROJECTILE));
    }
}
