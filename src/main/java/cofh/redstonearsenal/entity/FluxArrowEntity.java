package cofh.redstonearsenal.entity;

import cofh.redstonearsenal.item.IFluxItem;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_ARROW_ENTITY;

public class FluxArrowEntity extends AbstractArrowEntity {

    protected static final DataParameter<Byte> RSA_FLAGS = EntityDataManager.defineId(FluxArrowEntity.class, DataSerializers.BYTE);
    protected static final int LIFESPAN = 200;
    protected static final float EXPLOSION_RANGE = 4.0F;

    public FluxArrowEntity(EntityType<? extends FluxArrowEntity> entityIn, World worldIn) {

        super(entityIn, worldIn);
    }

    public FluxArrowEntity(World worldIn, LivingEntity shooter) {

        super(FLUX_ARROW_ENTITY, shooter, worldIn);
    }

    public FluxArrowEntity(World worldIn, double x, double y, double z) {

        super(FLUX_ARROW_ENTITY, x, y, z, worldIn);
    }

    @Override
    protected ItemStack getPickupItem() {

        return ItemStack.EMPTY;
    }

    public void setExplodeArrow(boolean explode) {

        setRSAFlag(1, explode);
    }

    public boolean isExplodeArrow() {

        return (this.entityData.get(RSA_FLAGS) & 1) != 0;
    }

    @Override
    protected void defineSynchedData() {

        super.defineSynchedData();
        this.entityData.define(RSA_FLAGS, (byte) 0);
    }

    private void setRSAFlag(int flag, boolean value) {

        byte b0 = this.entityData.get(RSA_FLAGS);
        if (value) {
            this.entityData.set(RSA_FLAGS, (byte) (b0 | flag));
        } else {
            this.entityData.set(RSA_FLAGS, (byte) (b0 & ~flag));
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public DamageSource getDamageSource(AbstractArrowEntity arrow, @Nullable Entity shooter) {

        return IFluxItem.fluxRangedDamage(arrow, shooter == null ? arrow : shooter);
    }

    public void explode(Vector3d pos) {

        if (!level.isClientSide()) {
            double r2 = EXPLOSION_RANGE * EXPLOSION_RANGE;
            AxisAlignedBB searchArea = this.getBoundingBox().move(pos.subtract(this.position())).inflate(EXPLOSION_RANGE, 1, EXPLOSION_RANGE);
            Predicate<Entity> filter = EntityPredicates.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof LivingEntity);
            for (Entity target : level.getEntities(this, searchArea, filter)) {
                if (pos.distanceToSqr(target.position()) < r2) {
                    target.hurt(getDamageSource(this, getOwner()), (float) getBaseDamage());
                }
            }
            level.broadcastEntityEvent(this, (byte) 3);
            remove();
        }
    }

    @Override
    public void tick() {

        if (!level.isClientSide() && tickCount > LIFESPAN) {
            level.broadcastEntityEvent(this, (byte) 3);
            remove();
        } else {
            super.tick();
        }
    }

    @Override
    protected float getWaterInertia() {

        return 1.0F;
    }

    @Override
    protected void onHit(RayTraceResult result) {

        if (isExplodeArrow()) {
            this.explode(result.getLocation());
        } else {
            RayTraceResult.Type type = result.getType();
            if (type == RayTraceResult.Type.ENTITY) {
                this.onHitEntity((EntityRayTraceResult) result);
            } else if (type == RayTraceResult.Type.BLOCK) {
                this.onHitBlock((BlockRayTraceResult) result);
            }
        }
    }

    @OnlyIn (Dist.CLIENT)
    public void handleEntityEvent(byte event) {

        if (event == 3 && this.level.isClientSide && isExplodeArrow()) {
            level.addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 0, 0, 0);
            level.playLocalSound(getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }
        super.handleEntityEvent(event);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {

        Entity entity = result.getEntity();
        float f = (float) this.getDeltaMovement().length();
        int i = MathHelper.ceil(MathHelper.clamp((double) f * this.baseDamage, 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.remove();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource = getDamageSource(this, entity1);
        if (entity1 instanceof LivingEntity) {
            ((LivingEntity) entity1).setLastHurtMob(entity);
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.setSecondsOnFire(5);
        }

        if (entity.hurt(damagesource, (float) i)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entity;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vector3d vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        livingentity.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!this.level.isClientSide && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (entity1 != null && livingentity != entity1 && livingentity instanceof PlayerEntity && entity1 instanceof ServerPlayerEntity && !this.isSilent()) {
                    ((ServerPlayerEntity) entity1).connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level.isClientSide && entity1 instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) entity1;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.remove();
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == PickupStatus.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.remove();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult rayTraceResult) {

        level.broadcastEntityEvent(this, (byte) 3);
        this.remove();
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {

        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("explode", this.isExplodeArrow());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {

        super.readAdditionalSaveData(nbt);
        this.setExplodeArrow(nbt.getBoolean("explode"));
    }

}
