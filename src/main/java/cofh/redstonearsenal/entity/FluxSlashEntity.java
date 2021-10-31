package cofh.redstonearsenal.entity;

import cofh.redstonearsenal.item.IFluxItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_SLASH_ENTITY;

public class FluxSlashEntity extends ProjectileEntity {

    public static final float DAMAGE = 2.0F;
    public static final float SPEED = 2.0F;
    public static final int DURATION = 5;
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
        this.shootFromRotation(livingEntityIn, livingEntityIn.xRot, livingEntityIn.yRot, 0.0F, SPEED, 0.5F);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {

        super.tick();
        if (this.tickCount > DURATION) {
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove();
        }

        this.calculateCollision(this.level);

        this.checkInsideBlocks();
        Vector3d velocity = this.getDeltaMovement();
        this.updateRotation();

        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
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
    protected void onHitEntity(EntityRayTraceResult result) {

        super.onHitEntity(result);
        result.getEntity().hurt(IFluxItem.fluxRangedDamage(this, this.getOwner()), DAMAGE);
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {

        super.onHitBlock(result);
        this.level.broadcastEntityEvent(this, (byte) 3);
        this.remove();
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
            }
            else if (blockstate.is(Blocks.END_GATEWAY)) {
                TileEntity tileentity = world.getBlockEntity(blockpos);
                if (tileentity instanceof EndGatewayTileEntity && EndGatewayTileEntity.canEntityTeleport(this)) {
                    ((EndGatewayTileEntity)tileentity).teleportEntity(this);
                }
            }
            else {
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

    @Nullable
    public void hitEntities(World world, Vector3d startPos, Vector3d endPos) {

        AxisAlignedBB searchArea = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D);
        for(Entity entity : world.getEntities(this, searchArea, this::canHitEntity)) {
            AxisAlignedBB hitbox = entity.getBoundingBox().inflate(this.getBbWidth() * 0.5, this.getBbHeight() * 0.5, this.getBbWidth() * 0.5);
            if (hitbox.clip(startPos, endPos).isPresent() && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, new EntityRayTraceResult(entity))) {
                this.onHitEntity(new EntityRayTraceResult(entity));
            }
        }
    }
}