package cofh.redstonearsenal.common.entity;

import cofh.core.common.entity.Shockwave;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static cofh.redstonearsenal.init.registries.ModEntities.SHOCKWAVE_ENTITY;

public class FluxHammerShockwave extends Shockwave {

    public FluxHammerShockwave(EntityType<? extends FluxHammerShockwave> type, Level level) {

        super(type, level);
    }

    public FluxHammerShockwave(Level level, Vec3 pos, LivingEntity attacker) {

        super(SHOCKWAVE_ENTITY.get(), level, pos, attacker, 8.0F);
    }

    @Override
    protected void onHit() {

        if (getOwner() instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 70, 0));
        }
    }

    @Override
    protected DamageSource getDamageSource() {

        Entity owner = getOwner();
        DamageSources sources = level.damageSources();
        if (owner instanceof Player player) {
            return sources.source(DamageTypes.PLAYER_ATTACK, this, player);
        }
        if (owner instanceof LivingEntity living) {
            return sources.source(DamageTypes.MOB_ATTACK, this, living);
        }
        return sources.source(DamageTypes.IN_WALL, this);
    }

}
