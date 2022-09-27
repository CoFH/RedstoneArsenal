package cofh.redstonearsenal.entity;

import cofh.lib.item.FishingRodItemCoFH;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import static cofh.redstonearsenal.init.RSAEntities.FISH_HOOK;

public class FluxFishingHook extends FishingHook implements IEntityAdditionalSpawnData {

    protected FluxFishingHook(EntityType<? extends FluxFishingHook> type, Player player, Level level, int luck, int lure) {

        super(type, level, luck, lure);
        this.setOwner(player);
        float f = player.getXRot();
        float f1 = player.getYRot();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        double d0 = player.getX() - (double) f3 * 0.3D;
        double d1 = player.getEyeY();
        double d2 = player.getZ() - (double) f2 * 0.3D;
        this.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double) (-f3), (double) Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double) (-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
        this.setDeltaMovement(vec3);
        this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public FluxFishingHook(EntityType<? extends FluxFishingHook> type, Level level) {

        super(type, level);
    }

    public FluxFishingHook(Player player, Level level, int luck, int lure) {

        this(FISH_HOOK.get(), player, level, luck, lure);
    }

    @Override
    public Packet<?> getAddEntityPacket() {

        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldStopFishing(Player player) {

        if (player.isAlive() && (player.getMainHandItem().getItem() instanceof FishingRodItemCoFH || player.getOffhandItem().getItem() instanceof FishingRodItemCoFH) && this.distanceToSqr(player) <= 1024) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {

        Entity owner = this.getOwner();
        buffer.writeInt(owner == null ? -1 : owner.getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {

        setOwner(level.getEntity(additionalData.readInt()));
    }

}
