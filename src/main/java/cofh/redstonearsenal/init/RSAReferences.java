package cofh.redstonearsenal.init;

import cofh.redstonearsenal.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ObjectHolder;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAIDs.*;

@ObjectHolder (ID_REDSTONE_ARSENAL)
public class RSAReferences {

    private RSAReferences() {

    }

    // region ENTITIES
    @ObjectHolder (ID_FLUX_SLASH)
    public static final EntityType<FluxSlash> FLUX_SLASH_ENTITY = null;
    @ObjectHolder (ID_FLUX_TRIDENT)
    public static final EntityType<ThrownFluxTrident> FLUX_TRIDENT_ENTITY = null;
    @ObjectHolder (ID_FLUX_WRENCH)
    public static final EntityType<ThrownFluxWrench> FLUX_WRENCH_ENTITY = null;
    @ObjectHolder (ID_FLUX_ARROW)
    public static final EntityType<FluxArrow> FLUX_ARROW_ENTITY = null;
    @ObjectHolder (ID_SHOCKWAVE)
    public static final EntityType<Shockwave> SHOCKWAVE_ENTITY = null;
    // endregion

    // region BLOCKS
    @ObjectHolder (ID_FLUX_GLOW_AIR)
    public static final Block FLUX_GLOW_AIR = null;
    @ObjectHolder (ID_FLUX_PATH)
    public static final Block FLUX_PATH = null;
    // endregion

    // region ITEMS
    //@ObjectHolder(ID_FLUX_SWORD)
    //public static final Item FLUX_SWORD_ITEM = null;
    // endregion

}
