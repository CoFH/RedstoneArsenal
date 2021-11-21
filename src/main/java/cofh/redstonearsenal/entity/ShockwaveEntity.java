package cofh.redstonearsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static cofh.lib.util.references.CoreReferences.SUNDERED;
import static cofh.redstonearsenal.init.RSAReferences.SHOCKWAVE_ENTITY;

public class ShockwaveEntity extends Entity {

    public static final int LIFESPAN = 10;
    public static final int ANIM_DURATION = 5;
    public static final float DISTANCE_PER_TICK = 1F;
    protected static final float BASE_DAMAGE = 6;
    protected static final int SUNDER_DURATION = 50;

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
            if (tickCount > LIFESPAN + ANIM_DURATION) {
                this.remove();
            }
            else if (tickCount < LIFESPAN) {
//                BlockPos origin = this.blockPosition();
//                List<int[]> offsets = offsetsByTick.get(tickCount);
//                for (int[] offset : offsets) {
//                    for (int y = 1; y >= -1; --y) {
//                        BlockPos pos = origin.offset(offset[0], y, offset[1]);
//                        BlockState state = level.getBlockState(pos);
//                        if (!state.isAir(level, pos) && state.isRedstoneConductor(level, pos) &&
//                                state.isCollisionShapeFullBlock(level, pos) && !state.hasTileEntity()) { // && !level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above())
//                            FallingBlockEntity entity = new FallingBlockEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, state);
//                            entity.setDeltaMovement(0, 0.1, 0);
//                            level.addFreshEntity(entity);
//                            break;
//                        }
//                    }
//                }
                float lower = Math.max((tickCount - 1) * DISTANCE_PER_TICK, 0);
                float upper = lower + DISTANCE_PER_TICK * 1.5F;
                float lowerSqr = lower * lower;
                float upperSqr = upper * upper;
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(upper + 1, 2, upper + 1).inflate(0.5), EntityPredicates.NO_CREATIVE_OR_SPECTATOR)) {
                    if (!entity.equals(this.owner)) {
                        Vector3d relPos = new Vector3d(entity.getX() - this.getX(), 0, entity.getZ() - this.getZ());
                        double distSqr = relPos.lengthSqr();
                        if (lowerSqr < distSqr && distSqr < upperSqr) {
                            float falloff = (LIFESPAN - (tickCount * 0.5F)) / LIFESPAN;
                            DamageSource source = DamageSource.mobAttack(this.owner);
                            if (this.owner instanceof PlayerEntity) {
                                source = DamageSource.playerAttack((PlayerEntity) this.owner);
                            }
                            entity.hurt(source, BASE_DAMAGE * falloff);
                            entity.addEffect(new EffectInstance(SUNDERED, SUNDER_DURATION, 0, false, false));
                            Vector3d knockback = relPos.scale(0.8 / MathHelper.sqrt(distSqr)).add(0, 0.3, 0).scale(1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                            entity.setDeltaMovement(knockback);
                        }
                    }
                }
            }
        }
        super.tick();
    }
}
