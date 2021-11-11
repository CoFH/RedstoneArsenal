package cofh.redstonearsenal.entity;

import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.item.FluxTridentItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_TRIDENT_ENTITY;

public class FluxTridentEntity extends AbstractArrowEntity {

    private static final DataParameter<Byte> ID_LOYALTY = EntityDataManager.defineId(TridentEntity.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> ID_FOIL = EntityDataManager.defineId(TridentEntity.class, DataSerializers.BOOLEAN);
    private ItemStack tridentItem = new ItemStack(RedstoneArsenal.ITEMS.get("flux_trident").getItem());
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public FluxTridentEntity(EntityType<? extends FluxTridentEntity> p_i50148_1_, World p_i50148_2_) {
        super(p_i50148_1_, p_i50148_2_);
    }

    public FluxTridentEntity(World p_i48790_1_, LivingEntity p_i48790_2_, ItemStack p_i48790_3_) {
        super(FLUX_TRIDENT_ENTITY, p_i48790_2_, p_i48790_1_);
        this.tridentItem = p_i48790_3_.copy();
        this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(p_i48790_3_));
        this.entityData.set(ID_FOIL, p_i48790_3_.hasFoil());
    }

    @OnlyIn(Dist.CLIENT)
    public FluxTridentEntity(World p_i48791_1_, double p_i48791_2_, double p_i48791_4_, double p_i48791_6_) {
        super(FLUX_TRIDENT_ENTITY, p_i48791_2_, p_i48791_4_, p_i48791_6_, p_i48791_1_);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte)0);
        this.entityData.define(ID_FOIL, false);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean isEmpowered() {

        return ((FluxTridentItem) tridentItem.getItem()).isEmpowered(tridentItem);
    }

    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        if ((this.dealtDamage || this.isNoPhysics()) && entity != null) {
            int i = this.entityData.get(ID_LOYALTY);
            if (i > 0 && !this.isAcceptableReturnOwner()) {
                if (!this.level.isClientSide && this.pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.remove();
            } else if (i > 0) {
                this.setNoPhysics(true);
                Vector3d vector3d = new Vector3d(entity.getX() - this.getX(), entity.getEyeY() - this.getY(), entity.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + vector3d.y * 0.015D * (double)i, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * (double)i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vector3d.normalize().scale(d0)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnTridentTickCount;
            }
        }

        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
        } else {
            return false;
        }
    }

    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Nullable
    protected EntityRayTraceResult findHitEntity(Vector3d p_213866_1_, Vector3d p_213866_2_) {
        return this.dealtDamage ? null : super.findHitEntity(p_213866_1_, p_213866_2_);
    }

    protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
        Entity entity = p_213868_1_.getEntity();
        float f = 8.0F;
        if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            f += EnchantmentHelper.getDamageBonus(this.tridentItem, livingentity.getMobType());
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource = DamageSource.trident(this, (Entity)(entity1 == null ? this : entity1));
        this.dealtDamage = true;
        SoundEvent soundevent = SoundEvents.TRIDENT_HIT;
        if (entity.hurt(damagesource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity1 = (LivingEntity)entity;
                if (entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity1);
                }

                this.doPostHurtEffects(livingentity1);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float f1 = 1.0F;
        if (this.level instanceof ServerWorld && this.level.isThundering() && EnchantmentHelper.hasChanneling(this.tridentItem)) {
            BlockPos blockpos = entity.blockPosition();
            if (this.level.canSeeSky(blockpos)) {
                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(this.level);
                lightningboltentity.moveTo(Vector3d.atBottomCenterOf(blockpos));
                lightningboltentity.setCause(entity1 instanceof ServerPlayerEntity ? (ServerPlayerEntity)entity1 : null);
                this.level.addFreshEntity(lightningboltentity);
                soundevent = SoundEvents.TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }

        this.playSound(soundevent, f1, 1.0F);
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    public void playerTouch(PlayerEntity p_70100_1_) {
        Entity entity = this.getOwner();
        if (entity == null || entity.getUUID() == p_70100_1_.getUUID()) {
            super.playerTouch(p_70100_1_);
        }
    }

    public void readAdditionalSaveData(CompoundNBT p_70037_1_) {
        super.readAdditionalSaveData(p_70037_1_);
        if (p_70037_1_.contains("Trident", 10)) {
            this.tridentItem = ItemStack.of(p_70037_1_.getCompound("Trident"));
        }

        this.dealtDamage = p_70037_1_.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, (byte)EnchantmentHelper.getLoyalty(this.tridentItem));
    }

    public void addAdditionalSaveData(CompoundNBT p_213281_1_) {
        super.addAdditionalSaveData(p_213281_1_);
        p_213281_1_.put("Trident", this.tridentItem.save(new CompoundNBT()));
        p_213281_1_.putBoolean("DealtDamage", this.dealtDamage);
    }

    public void tickDespawn() {
        int i = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrowEntity.PickupStatus.ALLOWED || i <= 0) {
            super.tickDespawn();
        }

    }

    protected float getWaterInertia() {
        return 0.99F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return true;
    }
}
