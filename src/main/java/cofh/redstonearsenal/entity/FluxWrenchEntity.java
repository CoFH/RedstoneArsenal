package cofh.redstonearsenal.entity;

import cofh.lib.util.Utils;
import cofh.lib.util.helpers.ArcheryHelper;
import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.item.FluxWrenchItem;
import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_WRENCH;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_WRENCH_ENTITY;

public class FluxWrenchEntity extends ProjectileEntity {

    protected static final DataParameter<ItemStack> DATA_ITEM_STACK = EntityDataManager.defineId(FluxWrenchEntity.class, DataSerializers.ITEM_STACK);
    public static final float SPEED = 1.3F;
    public static final float RANGE = 16.0F;
    public boolean hitSomething = false;

    public FluxWrenchEntity(EntityType<? extends ProjectileEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public FluxWrenchEntity(World worldIn, double x, double y, double z) {

        this(FLUX_WRENCH_ENTITY, worldIn);
        this.setPos(x, y, z);
    }

    public FluxWrenchEntity(World worldIn, LivingEntity livingEntityIn, ItemStack stackIn) {

        this(worldIn, livingEntityIn.getX(), livingEntityIn.getEyeY() - 0.1F, livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.setItem(stackIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, SPEED, 0.5F);
    }

    @Override
    protected void defineSynchedData() {

        this.getEntityData().define(DATA_ITEM_STACK, new ItemStack(getDefaultItem()));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected Item getDefaultItem() {

        return RedstoneArsenal.ITEMS.get(ID_FLUX_WRENCH).getItem();
    }

    protected ItemStack getItemRaw() {

        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    public ItemStack getItem() {

        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
    }

    public void setItem(ItemStack stack) {

        if (stack.getItem() != this.getDefaultItem() || stack.hasTag()) {
            this.getEntityData().set(DATA_ITEM_STACK, Util.make(stack.copy(), (itemStack) -> itemStack.setCount(1)));
        }
    }

    public boolean isEmpowered() {

        ItemStack stack = this.getItem();
        return ((FluxWrenchItem) stack.getItem()).isEmpowered(stack);
    }

    @Override
    public void tick() {

        super.tick();

        calculateCollision(this.level);
        checkInsideBlocks();

        if (!this.isAlive()) {
            return;
        }
        Vector3d velocity = getDeltaMovement();
        Entity owner = this.getOwner();
        if (owner != null) {
            Vector3d relPos = owner.getEyePosition(0).subtract(this.position());
            double distance = relPos.length();
            if (distance > RANGE) {
                this.hitSomething = true;
            }
            if (this.hitSomething) {
                if (distance < 1.5) {
                    returnToInventory();
                } else {
                    this.setDeltaMovement(relPos.scale(SPEED / distance));
                }
                //                Vector3d acceleration = relPos.scale(Math.min(1, 0.05 * tickCount) / distance);
                //                velocity = velocity.add(acceleration).normalize().scale(SPEED);
            }
        }

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                level.addParticle(ParticleTypes.BUBBLE, getX() + velocity.x * 0.75D, getY() + velocity.y * 0.75D, getZ() + velocity.z * 0.75D, velocity.x, velocity.y, velocity.z);
            }
        }

        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
    }

    public void calculateCollision(World world) {

        Vector3d start = this.position();
        Vector3d end = start.add(this.getDeltaMovement());
        BlockRayTraceResult blockResult = this.getBlockHitResult(world, start, end);
        boolean blockCollision = false;
        if (blockResult.getType() != RayTraceResult.Type.MISS) {
            end = blockResult.getLocation();
            BlockPos blockpos = blockResult.getBlockPos();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(blockpos);
            } else if (blockstate.is(Blocks.END_GATEWAY)) {
                TileEntity tileentity = world.getBlockEntity(blockpos);
                if (tileentity instanceof EndGatewayTileEntity && EndGatewayTileEntity.canEntityTeleport(this)) {
                    ((EndGatewayTileEntity) tileentity).teleportEntity(this);
                }
            } else {
                blockCollision = true;
            }
        }

        this.hitEntities(this.level, start, end);

        if (blockCollision && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, blockResult)) {
            this.onHitBlock(blockResult);
        }
    }

    public BlockRayTraceResult getBlockHitResult(World world, Vector3d startPos, Vector3d endPos) {

        return world.clip(new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
    }

    public void hitEntities(World world, Vector3d startPos, Vector3d endPos) {

        ArcheryHelper.findHitEntities(world, this, startPos, endPos, this::canHitEntity)
                .filter(result -> !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, result))
                .forEach(this::onHitEntity);
    }

    @Override
    protected void onHit(RayTraceResult result) {

        if (!this.hitSomething) {
            Entity owner = this.getOwner();
            if (owner != null) {
                Vector3d relPos = owner.getEyePosition(1).subtract(this.position());
                double distance = relPos.length();
                if (distance < 1.5) {
                    returnToInventory();
                } else {
                    this.setDeltaMovement(relPos.scale(SPEED * 0.5 / distance));
                }
            } else {
                this.setDeltaMovement(getDeltaMovement().scale(-0.5));
            }
            this.hitSomething = true;
        }
    }

    protected void returnToInventory() {

        Entity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        if (!(owner instanceof PlayerEntity && ((PlayerEntity) owner).inventory.add(this.getItem()))) {
            level.addFreshEntity(new ItemEntity(level, owner.getX(), owner.getY(), owner.getZ(), this.getItem()));
        }
        this.remove();
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {

        if (!this.isAlive() || level.isClientSide()) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target.equals(owner)) {
            this.returnToInventory();
        } else {
            if (result.getEntity().hurt(IFluxItem.fluxRangedDamage(this, owner), calculateDamage(target)) && target.getType() != EntityType.ENDERMAN) {
                int fireAspect = Utils.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, this.getItem());
                if (this.isOnFire() || fireAspect > 0) {
                    target.setSecondsOnFire(Math.max(this.isOnFire() ? 5 : 0, fireAspect * 4));
                }
            }
        }
        onHit(result);
    }

    protected float calculateDamage(Entity target) {

        ItemStack stack = this.getItem();
        Entity owner = this.getOwner();
        float damage = ((FluxWrenchItem) stack.getItem()).getRangedAttackDamage(stack);
        if (target instanceof LivingEntity) {
            damage += EnchantmentHelper.getDamageBonus(stack, ((LivingEntity) target).getMobType());
            if (owner instanceof LivingEntity) {
                stack.getItem().hurtEnemy(stack, (LivingEntity) target, (LivingEntity) owner);
            }
        } else {
            damage += EnchantmentHelper.getDamageBonus(stack, CreatureAttribute.UNDEFINED);
        }
        return damage;
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {

        if (!this.isAlive() || level.isClientSide()) {
            return;
        }
        BlockState state = this.level.getBlockState(result.getBlockPos());
        state.onProjectileHit(this.level, state, result, this);
        Entity owner = this.getOwner();
        ItemStack stack = this.getItem();
        if (owner instanceof PlayerEntity && stack.getItem() instanceof FluxWrenchItem) {
            ((FluxWrenchItem) stack.getItem()).useRanged(level, stack, (PlayerEntity) owner, result);
        }
        onHit(result);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {

        super.readAdditionalSaveData(nbt);

        ItemStack itemstack = ItemStack.of(nbt.getCompound("item"));
        this.setItem(itemstack);
        hitSomething = nbt.getBoolean("hit");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {

        super.addAdditionalSaveData(nbt);

        ItemStack itemstack = this.getItemRaw();
        if (!itemstack.isEmpty()) {
            nbt.put("item", itemstack.save(new CompoundNBT()));
        }
        nbt.putBoolean("hit", hitSomething);
    }

}
