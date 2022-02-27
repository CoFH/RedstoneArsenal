package cofh.redstonearsenal.entity;

import cofh.lib.entity.AbstractAoESpellEntity;
import cofh.lib.util.references.CoreReferences;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import static cofh.lib.util.references.CoreReferences.SUNDERED;
import static cofh.redstonearsenal.init.RSAReferences.SHOCKWAVE_ENTITY;

public class ShockwaveEntity extends AbstractAoESpellEntity {

    public static final float speed = 1.0F;

    public float damage = 8.0F;
    public int debuffDuration = 100;

    public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, World world) {

        super(type, world);
        radius = 8.0F;
        duration = MathHelper.ceil(radius / speed);
    }

    public ShockwaveEntity(World world, LivingEntity attacker, Vector3d pos, float yRot) {

        this(SHOCKWAVE_ENTITY, world);
        this.owner = attacker;
        setPos(pos.x(), pos.y(), pos.z());
        setRot(yRot, 0);
    }

    public ShockwaveEntity(World world, LivingEntity attacker, Vector3d pos, float yRot, int damageModifier) {

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
            owner.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 70, 0));
        }
    }

    public boolean attack() {

        boolean hitSomething = false;
        float lower = Math.max((tickCount - 1) * speed, 0);
        float upper = lower + speed * 1.5F;
        float lowerSqr = lower * lower;
        float upperSqr = upper * upper;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(upper + 1, 2, upper + 1).inflate(0.5), EntityPredicates.NO_CREATIVE_OR_SPECTATOR)) {
            if (!entity.equals(this.owner)) {
                Vector3d relPos = new Vector3d(entity.getX() - this.getX(), 0, entity.getZ() - this.getZ());
                double distSqr = relPos.lengthSqr();
                if (lowerSqr < distSqr && distSqr < upperSqr) {
                    float falloff = (duration - (tickCount * 0.5F)) / duration;
                    DamageSource source;
                    if (this.owner instanceof PlayerEntity) {
                        source = DamageSource.playerAttack((PlayerEntity) this.owner);
                    } else {
                        source = DamageSource.mobAttack(this.owner);
                    }
                    if (entity.hurt(source, damage * falloff)) {
                        hitSomething = true;
                        entity.addEffect(new EffectInstance(SUNDERED, debuffDuration, 0, false, false));
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
