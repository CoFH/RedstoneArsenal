package cofh.redstonearsenal.entity;

import cofh.core.util.references.CoreReferences;
import cofh.lib.entity.AbstractAoESpell;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static cofh.core.util.references.CoreReferences.SUNDERED;
import static cofh.redstonearsenal.init.RSAReferences.SHOCKWAVE_ENTITY;

public class Shockwave extends AbstractAoESpell {

    public static final float speed = 1.0F;

    public float damage = 8.0F;
    public int debuffDuration = 100;

    public Shockwave(EntityType<? extends Shockwave> type, Level world) {

        super(type, world);
        radius = 8.0F;
        duration = MathHelper.ceil(radius / speed);
    }

    public Shockwave(Level world, LivingEntity attacker, Vec3 pos, float yRot) {

        this(SHOCKWAVE_ENTITY, world);
        this.owner = attacker;
        setPos(pos.x(), pos.y(), pos.z());
        setRot(yRot, 0);
    }

    public Shockwave(Level world, LivingEntity attacker, Vec3 pos, float yRot, int damageModifier) {

        this(world, attacker, pos, yRot);
        damage += damageModifier;
    }

    @Override
    public void onCast() {

        if (level.isClientSide) {
            BlockPos pos = this.blockPosition();
            level.addParticle(CoreReferences.SHOCKWAVE_PARTICLE, pos.getX(), pos.getY(), pos.getZ(), speed, radius, 0.6D);
        }
    }

    @Override
    public void activeTick() {

        if (!level.isClientSide && attack()) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 70, 0));
        }
    }

    public boolean attack() {

        boolean hitSomething = false;
        float lower = Math.max((tickCount - 1) * speed, 0);
        float upper = lower + speed * 1.5F;
        float lowerSqr = lower * lower;
        float upperSqr = upper * upper;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(upper + 1, 2, upper + 1).inflate(0.5), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (!entity.equals(this.owner)) {
                Vec3 relPos = new Vec3(entity.getX() - this.getX(), 0, entity.getZ() - this.getZ());
                double distSqr = relPos.lengthSqr();
                if (lowerSqr < distSqr && distSqr < upperSqr) {
                    float falloff = (duration - (tickCount * 0.5F)) / duration;
                    DamageSource source;
                    if (this.owner instanceof Player) {
                        source = DamageSource.playerAttack((Player) this.owner);
                    } else {
                        source = DamageSource.mobAttack(this.owner);
                    }
                    if (entity.hurt(source, damage * falloff)) {
                        hitSomething = true;
                        entity.addEffect(new MobEffectInstance(SUNDERED, debuffDuration, 0, false, false));
                        entity.knockback(0.8F, -relPos.x(), -relPos.z());
                        //Vector3d knockback = relPos.scale(0.8 / MathHelper.sqrt(distSqr)).add(0, 0.3, 0).scale(1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        //entity.setDeltaMovement(knockback);
                    }
                }
            }
        }
        return hitSomething;
    }

}
