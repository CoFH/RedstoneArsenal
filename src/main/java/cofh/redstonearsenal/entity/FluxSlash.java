package cofh.redstonearsenal.entity;

import cofh.core.util.helpers.ArcheryHelper;
import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import static cofh.redstonearsenal.init.RSAEntities.FLUX_SLASH;

public class FluxSlash extends Projectile {

    public static float defaultSpeed = 2.0F;

    public float damage = 2.0F;
    public int duration = 5;

    public final float zRot;

    public FluxSlash(EntityType<? extends Projectile> type, Level worldIn) {

        super(type, worldIn);
        zRot = (worldIn.getRandom().nextFloat() - 0.5F) * 50;
    }

    public FluxSlash(Level worldIn, double x, double y, double z) {

        this(FLUX_SLASH.get(), worldIn);
        this.setPos(x, y, z);
    }

    public FluxSlash(Level worldIn, LivingEntity livingEntityIn) {

        this(worldIn, livingEntityIn.getX(), 0.7 * livingEntityIn.getEyeY() + 0.3 * livingEntityIn.getY(), livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, defaultSpeed, 0.5F);
    }

    public FluxSlash(Level worldIn, LivingEntity livingEntityIn, int damageModifier) {

        this(worldIn, livingEntityIn);
        this.damage += damageModifier;
    }

    public FluxSlash(Level worldIn, LivingEntity livingEntityIn, int damageModifier, int durationModifier) {

        this(worldIn, livingEntityIn, damageModifier);
        this.duration += durationModifier;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void doWaterSplashEffect() {

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        super.onHitEntity(result);
        result.getEntity().hurt(IFluxItem.fluxRangedDamage(this, this.getOwner()), damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        super.onHitBlock(result);
        discard();
    }

    @Override
    public void tick() {

        super.tick();

        if (tickCount > duration) {
            discard();
        }
        calculateCollision(this.level);
        checkInsideBlocks();
        Vec3 velocity = getDeltaMovement();
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
    }

    @Override
    public Packet<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @OnlyIn (Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double p_70112_1_) {

        double d0 = this.getBoundingBox().getSize() * 10.0D;

        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }
        d0 = d0 * 64.0D * getViewScale();
        return p_70112_1_ < d0 * d0;
    }

    protected void calculateCollision(Level world) {

        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        BlockHitResult blockResult = this.getBlockHitResult(world, start, end);
        boolean blockCollision = false;
        if (blockResult.getType() != HitResult.Type.MISS) {
            end = blockResult.getLocation();
            blockCollision = true;
        }
        this.hitEntities(this.level, start, end);

        if (blockCollision && !ForgeEventFactory.onProjectileImpact(this, blockResult)) {
            this.onHitBlock(blockResult);
        }
    }

    protected BlockHitResult getBlockHitResult(Level world, Vec3 startPos, Vec3 endPos) {

        return world.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    protected void hitEntities(Level world, Vec3 startPos, Vec3 endPos) {

        ArcheryHelper.findHitEntities(world, this, startPos, endPos, this::canHitEntity)
                .filter(result -> !ForgeEventFactory.onProjectileImpact(this, result))
                .forEach(this::onHitEntity);
    }

}
