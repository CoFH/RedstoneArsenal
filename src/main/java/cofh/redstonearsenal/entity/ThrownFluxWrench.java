package cofh.redstonearsenal.entity;

import cofh.lib.util.Utils;
import cofh.lib.util.helpers.ArcheryHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.references.CoreReferences;
import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.item.FluxWrenchItem;
import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.Util;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_WRENCH;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_WRENCH_ENTITY;

public class ThrownFluxWrench extends Projectile implements IEntityAdditionalSpawnData {

    protected static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrownFluxWrench.class, EntityDataSerializers.ITEM_STACK);

    public float speed = 1.5F;
    public float range = 16.0F;
    public boolean hitSomething = false;

    public ThrownFluxWrench(EntityType<? extends Projectile> type, Level worldIn) {

        super(type, worldIn);
    }

    public ThrownFluxWrench(Level worldIn, double x, double y, double z) {

        this(FLUX_WRENCH_ENTITY, worldIn);
        this.setPos(x, y, z);
    }

    public ThrownFluxWrench(Level worldIn, LivingEntity livingEntityIn, ItemStack stackIn) {

        this(worldIn, livingEntityIn.getX(), livingEntityIn.getEyeY() - 0.1F, livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.setItem(stackIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, speed, 0.5F);
    }

    @Override
    protected void defineSynchedData() {

        this.getEntityData().define(DATA_ITEM_STACK, new ItemStack(getDefaultItem()));
    }

    @Override
    public Packet<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected Item getDefaultItem() {

        return RedstoneArsenal.ITEMS.get(ID_FLUX_WRENCH).asItem();
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
        Vec3 velocity = getDeltaMovement();
        Entity owner = this.getOwner();
        if (owner != null) {
            Vec3 relPos = owner.getEyePosition(0).subtract(this.position());
            double distance = relPos.length();
            if (distance > range) {
                if (isEmpowered()) {
                    teleportEffects(owner);
                    returnToInventory(owner);
                    return;
                }
                this.hitSomething = true;
            }
            if (this.hitSomething) {
                if (distance < 1.5) {
                    returnToInventory(owner);
                } else {
                    this.setDeltaMovement(relPos.scale(speed / distance));
                }
                //Vector3d acceleration = relPos.scale(Math.min(1, 0.05 * tickCount) / distance);
                //velocity = velocity.add(acceleration).normalize().scale(SPEED);
            }
        }

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                level.addParticle(ParticleTypes.BUBBLE, getX() + velocity.x * 0.75D, getY() + velocity.y * 0.75D, getZ() + velocity.z * 0.75D, velocity.x, velocity.y, velocity.z);
            }
        }

        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
    }

    public void calculateCollision(Level world) {

        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        BlockHitResult blockResult = this.getBlockHitResult(world, start, end);
        boolean blockCollision = false;
        if (blockResult.getType() != HitResult.Type.MISS) {
            end = blockResult.getLocation();
            BlockPos blockpos = blockResult.getBlockPos();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(blockpos);
            } else if (blockstate.is(Blocks.END_GATEWAY)) {
                BlockEntity blockentity = this.level.getBlockEntity(blockpos);
                if (blockentity instanceof TheEndGatewayBlockEntity && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                    TheEndGatewayBlockEntity.teleportEntity(this.level, blockpos, blockstate, this, (TheEndGatewayBlockEntity) blockentity);
                }
            } else {
                blockCollision = true;
            }
        }
        this.hitEntities(this.level, start, end);
        if (blockCollision && !ForgeEventFactory.onProjectileImpact(this, blockResult)) {
            this.onHitBlock(blockResult);
        }
    }

    public BlockHitResult getBlockHitResult(Level world, Vec3 startPos, Vec3 endPos) {

        return world.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    public void hitEntities(Level world, Vec3 startPos, Vec3 endPos) {

        ArcheryHelper.findHitEntities(world, this, startPos, endPos, this::canHitEntity)
                .filter(result -> !ForgeEventFactory.onProjectileImpact(this, result))
                .forEach(this::onHitEntity);
    }

    @Override
    protected void onHit(HitResult result) {

        if (!this.hitSomething) {
            Entity owner = this.getOwner();
            if (owner != null) {
                Vec3 pos = position();
                if (isEmpowered()) {
                    teleportEffects(owner);
                    returnToInventory(owner);
                } else {
                    Vec3 relPos = owner.getEyePosition(1).subtract(pos);
                    double distance = relPos.length();
                    if (distance < 1.5) {
                        returnToInventory(owner);
                    } else {
                        this.setDeltaMovement(relPos.scale(speed * 0.5 / distance));
                    }
                }
            } else {
                this.setDeltaMovement(getDeltaMovement().scale(-0.5));
            }
            this.hitSomething = true;
        }
    }

    protected void returnToInventory(Entity owner) {

        if (!(owner instanceof Player player && player.inventory.add(this.getItem()))) {
            level.addFreshEntity(new ItemEntity(level, owner.getX(), owner.getY(), owner.getZ(), this.getItem()));
        }
        this.discard();
    }

    protected void teleportEffects(Entity owner) {

        if (level.isClientSide) {
            return;
        }
        Vec3 pos = position();
        AABB bounds = getBoundingBox();
        ((ServerLevel) level).sendParticles(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 10, bounds.getXsize() * 0.4, bounds.getYsize() * 0.5, bounds.getZsize() * 0.4, 0);
        pos = owner.position();
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.BLOCKS, 0.5F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public void handleEntityEvent(byte event) {

        if (event == 3) {
            Vec3 pos = position();
            level.addParticle(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 0, 0, 0);
            Entity owner = getOwner();
            if (owner != null) {
                //Vec3 disp = owner.position().add(0, owner.getBbHeight() * 0.5F, 0).subtract(pos);
                //int n = MathHelper.floor(disp.length() * 0.5F);
                //disp = disp.scale(1.0F / n);
                //for (int i = 0; i < n; ++i) {
                //    pos = pos.add(disp);
                //}
            }
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        if (!this.isAlive() || level.isClientSide()) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target.equals(owner)) {
            this.returnToInventory(owner);
        } else {
            if (target.hurt(IFluxItem.fluxRangedDamage(this, owner), calculateDamage(target)) && target.getType() != EntityType.ENDERMAN) {
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
            damage += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
        }
        return damage;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        if (!this.isAlive() || level.isClientSide()) {
            return;
        }
        BlockState state = this.level.getBlockState(result.getBlockPos());
        state.onProjectileHit(this.level, state, result, this);
        Entity owner = this.getOwner();
        ItemStack stack = this.getItem();
        if (owner instanceof Player && stack.getItem() instanceof FluxWrenchItem wrench) {
            wrench.useRanged(level, stack, (Player) owner, result);
        }
        onHit(result);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {

        super.readAdditionalSaveData(nbt);

        ItemStack itemstack = ItemStack.of(nbt.getCompound("item"));
        this.setItem(itemstack);
        hitSomething = nbt.getBoolean("hit");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {

        super.addAdditionalSaveData(nbt);

        ItemStack itemstack = this.getItemRaw();
        if (!itemstack.isEmpty()) {
            nbt.put("item", itemstack.save(new CompoundTag()));
        }
        nbt.putBoolean("hit", hitSomething);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {

        Entity owner = this.getOwner();
        buffer.writeInt(owner == null ? -1 : owner.getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {

        int id = additionalData.readInt();
        if (id >= 0) {
            setOwner(level.getEntity(id));
        }
    }

}
