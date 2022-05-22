package cofh.redstonearsenal.entity;

import cofh.lib.util.helpers.MathHelper;
import cofh.redstonearsenal.item.IFluxItem;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Arrays;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_ARROW_ENTITY;

public class FluxArrow extends AbstractArrow {

    protected static final EntityDataAccessor<Byte> RSA_FLAGS = SynchedEntityData.defineId(FluxArrow.class, EntityDataSerializers.BYTE);

    protected static final int LIFESPAN = 200;
    protected static final float EXPLOSION_RANGE = 4.0F;

    public FluxArrow(EntityType<? extends FluxArrow> entityIn, Level worldIn) {

        super(entityIn, worldIn);
    }

    public FluxArrow(Level worldIn, LivingEntity shooter) {

        super(FLUX_ARROW_ENTITY, shooter, worldIn);
    }

    public FluxArrow(Level worldIn, double x, double y, double z) {

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
    public Packet<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public DamageSource getDamageSource(AbstractArrow arrow, @Nullable Entity shooter) {

        return IFluxItem.fluxRangedDamage(arrow, shooter == null ? arrow : shooter);
    }

    public void explode(Vec3 pos) {

        if (!level.isClientSide()) {
            ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.5F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
            double r2 = EXPLOSION_RANGE * EXPLOSION_RANGE;
            AABB searchArea = this.getBoundingBox().move(pos.subtract(this.position())).inflate(EXPLOSION_RANGE);
            for (Entity target : level.getEntities(this, searchArea, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                if (pos.distanceToSqr(target.getBoundingBox().getCenter()) < r2) {
                    target.hurt(getDamageSource(this, getOwner()), (float) getBaseDamage());
                }
            }
            discard();
        }
    }

    @Override
    public void tick() {

        if (!level.isClientSide() && tickCount > LIFESPAN) {
            discard();
        } else {
            super.tick();
            //if (!this.leftOwner) {
            //    this.leftOwner = this.checkLeftOwner();
            //}
            //if (!this.level.isClientSide) {
            //    this.setSharedFlag(6, this.isGlowing());
            //}
            //this.baseTick();
            //
            //boolean noPhysics = this.isNoPhysics();
            //
            //BlockPos blockPos = this.blockPosition();
            //BlockState state = this.level.getBlockState(blockPos);
            //if (!state.isAir(this.level, blockPos) && !noPhysics) {
            //    VoxelShape voxelshape = state.getCollisionShape(this.level, blockPos);
            //    if (!voxelshape.isEmpty()) {
            //        Vector3d vector3d1 = this.position();
            //
            //        for(AxisAlignedBB axisalignedbb : voxelshape.toAabbs()) {
            //            if (axisalignedbb.move(blockPos).contains(vector3d1)) {
            //                this.inGround = true;
            //                break;
            //            }
            //        }
            //    }
            //}
            //
            //Vector3d currPos = this.position();
            //Vector3d nextPos = currPos.add(this.getDeltaMovement());
            //RayTraceResult blockResult = this.level.clip(new RayTraceContext(currPos, nextPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            //if (blockResult.getType() != RayTraceResult.Type.MISS) {
            //    nextPos = blockResult.getLocation();
            //}
            ////TODO: account for bouncing off invuln enemies
            //for (EntityRayTraceResult entityResult : ArcheryHelper.findHitEntities(this.level, this, currPos, nextPos, this::canHitEntity).sorted(Comparator.comparingDouble(result -> result.getLocation().distanceToSqr(currPos))).collect(Collectors.toList())) {
            //    if (!this.isAlive()) {
            //        return;
            //    }
            //    Entity target = entityResult.getEntity();
            //    Entity owner = this.getOwner();
            //    if (target instanceof Player && owner instanceof Player && !((Player)owner).canHarmPlayer((Player)target)) {
            //        break;
            //    }
            //    if (!noPhysics && !ForgeEventFactory.onProjectileImpact(this, entityResult)) {
            //        this.onHit(entityResult);
            //        this.hasImpulse = true;
            //    }
            //    if (this.getPierceLevel() <= 0) {
            //        break;
            //    }
            //}
            //if (this.isAlive() && blockResult.getType() != RayTraceResult.Type.MISS) {
            //    this.onHit(blockResult);
            //}
            //
            //Vector3d velocity = this.getDeltaMovement();
            //double xVel = velocity.x;
            //double yVel = velocity.y;
            //double zVel = velocity.z;
            //if (this.isCritArrow()) {
            //    for(int i = 0; i < 4; ++i) {
            //        this.level.addParticle(ParticleTypes.CRIT, this.getX() + xVel * i * 0.25F, this.getY() + yVel * i * 0.25F, this.getZ() + zVel * i * 0.25F, -xVel, -yVel + 0.2D, -zVel);
            //    }
            //}
            //if (this.isInWater()) {
            //    for(int i = 0; i < 4; ++i) {
            //        this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + xVel * i * 0.25F, this.getY() + yVel * i * 0.25F, this.getZ() + zVel * i * 0.25F, xVel, yVel, zVel);
            //    }
            //}
            //
            //float radToDeg = 180F / (float) Math.PI;
            //float horzSpeed = MathHelper.sqrt(xVel * xVel + zVel * zVel);
            //if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            //    this.yRotO = this.yRot;
            //    this.xRotO = this.xRot;
            //}
            //this.xRot = lerpRotation(this.xRotO, (float) MathHelper.atan2(yVel, horzSpeed) * radToDeg);
            //this.yRot = lerpRotation(this.yRotO, (float)(MathHelper.atan2(xVel, zVel)) * radToDeg);
            //
            //this.setDeltaMovement(velocity.scale(this.isInWater() ? this.getWaterInertia() : 0.99F).subtract(0, getGravity(), 0));
            //this.setPos(nextPos.x, nextPos.y, nextPos.z);
            //this.checkInsideBlocks();
        }
    }

    public float getGravity() {

        return this.isNoGravity() || this.noPhysics ? 0.0F : 0.05F;
    }

    @Override
    public byte getPierceLevel() {

        return isExplodeArrow() ? 0 : super.getPierceLevel();
    }

    @Override
    protected float getWaterInertia() {

        return 0.99F;
    }

    @Override
    protected void onHit(HitResult result) {

        if (isExplodeArrow()) {
            explode(result.getLocation());
        } else {
            HitResult.Type type = result.getType();
            if (type == HitResult.Type.ENTITY) {
                this.onHitEntity((EntityHitResult) result);
            } else if (type == HitResult.Type.BLOCK) {
                this.onHitBlock((BlockHitResult) result);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        Entity target = result.getEntity();
        float speed = (float) this.getDeltaMovement().length();
        int dmg = MathHelper.ceil(MathHelper.clamp(speed * this.baseDamage, 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }
            this.piercingIgnoreEntityIds.add(target.getId());
        }
        if (this.isCritArrow()) {
            dmg = Math.min(this.random.nextInt(dmg / 2 + 2) + dmg, Integer.MAX_VALUE);
        }
        Entity owner = this.getOwner();
        DamageSource dmgSource = getDamageSource(this, owner);
        if (owner instanceof LivingEntity) {
            ((LivingEntity) owner).setLastHurtMob(target);
        }
        boolean canHurt = canHurtEntity(target);
        if (target.hurt(dmgSource, (float) dmg)) {
            if (!canHurt) {
                return;
            }
            if (target instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) target;
                if (this.knockback > 0) {
                    Vec3 vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        living.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }
                if (!this.level.isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(living, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, living);
                }
                this.doPostHurtEffects(living);
                if (living != owner && living instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
                if (!target.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(living);
                }
                if (!this.level.isClientSide && owner instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer) owner;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, this.piercedAndKilledEntities);
                    } else if (!target.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, Arrays.asList(target));
                    }
                }
            }
            this.playSound(getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                this.discard();
            }
        }
    }

    protected boolean canHurtEntity(Entity entity) {

        return entity.getType() != EntityType.ENDERMAN;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        if (isExplodeArrow()) {
            this.explode(result.getLocation());
            return;
        }
        level.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {

        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("explode", this.isExplodeArrow());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {

        super.readAdditionalSaveData(nbt);
        this.setExplodeArrow(nbt.getBoolean("explode"));
    }

}
