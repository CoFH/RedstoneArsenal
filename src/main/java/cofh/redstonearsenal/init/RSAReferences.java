package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.FluxSlashEntity;
import cofh.redstonearsenal.entity.FluxTridentEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.lib.util.references.CoreIDs.ID_GLOW_AIR;
import static cofh.redstonearsenal.init.RSAIDs.*;

@ObjectHolder(ID_REDSTONE_ARSENAL)
public class RSAReferences {


    private RSAReferences() {

    }

    // region ENTITIES
    @ObjectHolder(ID_FLUX_SLASH)
    public static final EntityType<FluxSlashEntity> FLUX_SLASH_ENTITY = null;
    @ObjectHolder(ID_FLUX_TRIDENT)
    public static final EntityType<FluxTridentEntity> FLUX_TRIDENT_ENTITY = null;
    // endregion

    // region BLOCKS
    @ObjectHolder(ID_FLUX_GLOW_AIR)
    public static final Block FLUX_GLOW_AIR = null;
    // endregion

    // region ITEMS
//    @ObjectHolder(ID_FLUX_SWORD)
//    public static final Item FLUX_SWORD_ITEM = null;
    // endregion
}
