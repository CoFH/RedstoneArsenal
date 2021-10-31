package cofh.redstonearsenal.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.world.World;

public class FluxBobberEntity extends FishingBobberEntity {

    public FluxBobberEntity(World p_i47290_1_, PlayerEntity p_i47290_2_, double p_i47290_3_, double p_i47290_5_, double p_i47290_7_) {
        //TODO: update super with flux bobber entity type
        super(p_i47290_1_, p_i47290_2_, p_i47290_3_, p_i47290_5_, p_i47290_7_);
    }

    public FluxBobberEntity(PlayerEntity p_i50220_1_, World p_i50220_2_, int p_i50220_3_, int p_i50220_4_) {
        super(p_i50220_1_, p_i50220_2_, p_i50220_3_, p_i50220_4_);
    }
}
