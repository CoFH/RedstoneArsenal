package cofh.redstonearsenal.entity;

import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.item.FluxWrenchItem;
import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_WRENCH;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_SLASH_ENTITY;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_WRENCH_ENTITY;

public class FluxWrenchEntity extends ProjectileItemEntity {

    public static final float SPEED = 1.3F;
    public static final float RANGE = 16.0F;
    public boolean hitSomething = false;

    public FluxWrenchEntity(EntityType<? extends ProjectileItemEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public FluxWrenchEntity(World worldIn, double x, double y, double z) {

        this(FLUX_WRENCH_ENTITY, worldIn);
        this.setPos(x, y, z);
    }

    public FluxWrenchEntity(World worldIn, LivingEntity livingEntityIn, ItemStack stackIn) {

        this(worldIn, livingEntityIn.getX(), livingEntityIn.getEyeY() - 0.1F , livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.setItem(stackIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, SPEED, 0.5F);
    }

    @Override
    protected void defineSynchedData() {

        super.defineSynchedData();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {

        return RedstoneArsenal.ITEMS.get(ID_FLUX_WRENCH).getItem();
    }

    @Override
    public void tick() {

        Vector3d velocity = this.getDeltaMovement();
        Entity owner = this.getOwner();
        if (owner != null) {
            Vector3d relPos = owner.getEyePosition(0.5F).subtract(this.position());
            double distance = relPos.length();
            if (this.hitSomething) {
                Vector3d acceleration = relPos.scale(0.5F / distance);
                velocity = velocity.add(acceleration).normalize().scale(SPEED);
                this.setDeltaMovement(velocity);
            }
            else if (distance > RANGE) {
                this.hitSomething = true;
                this.setDeltaMovement(getDeltaMovement().reverse());
            }
        }
        super.tick();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double p_70112_1_) {
        double d0 = this.getBoundingBox().getSize() * 10.0D;
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getViewScale();
        return p_70112_1_ < d0 * d0;
    }

    @Override
    public boolean isNoGravity() {

        return true;
    }

    @Override
    protected void updateRotation() {

    }

    @Override
    protected void onHit(RayTraceResult result) {

        this.hitSomething = true;
        super.onHit(result);
        this.setDeltaMovement(getDeltaMovement().reverse());
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {

        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target.equals(owner)) {
            if (!(target instanceof PlayerEntity && ((PlayerEntity) target).inventory.add(this.getItem()))) {
                level.addFreshEntity(new ItemEntity(level, target.getX(), target.getY(), target.getZ(), this.getItem()));
            }
            this.remove();
        }
        else {
            ItemStack stack = this.getItem();
            float damage = ((FluxWrenchItem) stack.getItem()).getRangedAttackDamage(stack);
            if (target instanceof LivingEntity) {
                damage += EnchantmentHelper.getDamageBonus(stack, ((LivingEntity) target).getMobType());
                if (owner instanceof LivingEntity) {
                    stack.getItem().hurtEnemy(stack, (LivingEntity) target, (LivingEntity) owner);
                }
            }
            else {
                damage += EnchantmentHelper.getDamageBonus(stack, CreatureAttribute.UNDEFINED);
            }
            result.getEntity().hurt(IFluxItem.fluxRangedDamage(this, owner), damage);
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {

        BlockState state = this.level.getBlockState(result.getBlockPos());
        state.onProjectileHit(this.level, state, result, this);
        Entity owner = this.getOwner();
        ItemStack stack = this.getItem();
        if (owner instanceof PlayerEntity && stack.getItem() instanceof FluxWrenchItem) {
            ((FluxWrenchItem) stack.getItem()).useRanged(level, stack, (PlayerEntity) owner, result);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {

        super.readAdditionalSaveData(compound);

        hitSomething = compound.getBoolean("hit");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {

        super.addAdditionalSaveData(compound);

        compound.putBoolean("hit", hitSomething);
    }
}
