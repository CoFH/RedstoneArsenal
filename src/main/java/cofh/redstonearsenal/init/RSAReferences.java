package cofh.redstonearsenal.init;


import cofh.redstonearsenal.entity.FluxSlashEntity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAIDs.*;

@ObjectHolder(ID_REDSTONE_ARSENAL)
public class RSAReferences {


    private RSAReferences() {

    }

    // region ENTITIES
    @ObjectHolder(ID_SWORD_PROJECTILE)
    public static final EntityType<FluxSlashEntity> SWORD_PROJECTILE_ENTITY = null;
    // endregion

    // region ITEMS
//    @ObjectHolder(ID_FLUX_SWORD)
//    public static final Item FLUX_SWORD_ITEM = null;
    // endregion
}
