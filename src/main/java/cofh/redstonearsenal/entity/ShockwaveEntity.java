package cofh.redstonearsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static cofh.lib.util.references.CoreReferences.SUNDERED;
import static cofh.redstonearsenal.init.RSAReferences.SHOCKWAVE_ENTITY;

public class ShockwaveEntity extends Entity {

    public static final float defaultSpeed = 1.0F;
    public static final int duration = 10;
    public static final int animDuration = 5;

    public float damage = 8.0F;
    public int debuffDuration = 100;

    protected LivingEntity owner = null;

    public ShockwaveEntity(EntityType<?> type, World world) {

        super(type, world);
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
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {

        if (!level.isClientSide()) {
            if (tickCount > duration + animDuration) {
                this.remove();
            } else if (tickCount < duration && attack()) {
                owner.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 50, 0));
                //owner.setSprinting(true);
            }
        }
        super.tick();
    }

    public boolean attack() {

        boolean hitSomething = false;
        float lower = Math.max((tickCount - 1) * defaultSpeed, 0);
        float upper = lower + defaultSpeed * 1.5F;
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
                        Vector3d knockback = relPos.scale(0.8 / MathHelper.sqrt(distSqr)).add(0, 0.3, 0).scale(1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        entity.setDeltaMovement(knockback);
                    }
                }
            }
        }
        return hitSomething;
    }

}
