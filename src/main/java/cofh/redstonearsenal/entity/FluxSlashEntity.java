package cofh.redstonearsenal.entity;

import cofh.lib.util.helpers.ArcheryHelper;
import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_SLASH_ENTITY;

public class FluxSlashEntity extends ProjectileEntity {

    public static float defaultSpeed = 2.0F;
    public static int defaultDuration = 5;

    public float damage = 2.0F;
    public int duration = defaultDuration;

    public final float zRot;

    public FluxSlashEntity(EntityType<? extends ProjectileEntity> type, World worldIn) {

        super(type, worldIn);

        zRot = (worldIn.getRandom().nextFloat() - 0.5F) * 50;
    }

    public FluxSlashEntity(World worldIn, double x, double y, double z) {

        this(FLUX_SLASH_ENTITY, worldIn);
        this.setPos(x, y, z);
    }

    public FluxSlashEntity(World worldIn, LivingEntity livingEntityIn) {

        this(worldIn, livingEntityIn.getX(), 0.7 * livingEntityIn.getEyeY() + 0.3 * livingEntityIn.getY(), livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, defaultSpeed, 0.5F);
    }

    public FluxSlashEntity(World worldIn, LivingEntity livingEntityIn, int enchantModifier) {

        this(worldIn, livingEntityIn);
        this.damage += enchantModifier;
        this.duration += enchantModifier;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void doWaterSplashEffect() {

    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {

        super.onHitEntity(result);
        result.getEntity().hurt(IFluxItem.fluxRangedDamage(this, this.getOwner()), damage);
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {

        super.onHitBlock(result);
        remove();
    }

    @Override
    public void tick() {

        super.tick();

        if (tickCount > duration) {
            level.broadcastEntityEvent(this, (byte) 3);
            remove();
        }
        calculateCollision(this.level);

        checkInsideBlocks();
        Vector3d velocity = getDeltaMovement();
        updateRotation();

        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
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

    protected void calculateCollision(World world) {

        Vector3d start = this.position();
        Vector3d end = start.add(this.getDeltaMovement());
        BlockRayTraceResult blockResult = this.getBlockHitResult(world, start, end);
        boolean blockCollision = false;
        if (blockResult.getType() != RayTraceResult.Type.MISS) {
            end = blockResult.getLocation();
            blockCollision = true;
        }
        this.hitEntities(this.level, start, end);

        if (blockCollision && !ForgeEventFactory.onProjectileImpact(this, blockResult)) {
            this.onHitBlock(blockResult);
        }
    }

    protected BlockRayTraceResult getBlockHitResult(World world, Vector3d startPos, Vector3d endPos) {

        return world.clip(new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
    }

    protected void hitEntities(World world, Vector3d startPos, Vector3d endPos) {

        ArcheryHelper.findHitEntities(world, this, startPos, endPos, this::canHitEntity)
                .filter(result -> !ForgeEventFactory.onProjectileImpact(this, result))
                .forEach(this::onHitEntity);
    }

}
