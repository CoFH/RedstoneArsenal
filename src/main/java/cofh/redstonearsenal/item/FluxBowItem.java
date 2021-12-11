package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.capability.IArcheryAmmoItem;
import cofh.lib.capability.IArcheryBowItem;
import cofh.lib.capability.templates.ArcheryAmmoItemWrapper;
import cofh.lib.capability.templates.ArcheryBowItemWrapper;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.impl.BowItemCoFH;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.ArcheryHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.capability.CapabilityArchery.AMMO_ITEM_CAPABILITY;
import static cofh.lib.capability.CapabilityArchery.BOW_ITEM_CAPABILITY;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.references.EnsorcReferences.TRUESHOT;
import static cofh.lib.util.references.EnsorcReferences.VOLLEY;
import static net.minecraft.enchantment.Enchantments.*;
import static net.minecraft.enchantment.Enchantments.FLAMING_ARROWS;

public class FluxBowItem extends BowItemCoFH implements IFluxItem {

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxBowItem(int enchantability, float accuracyModifier, float damageModifier, float velocityModifier, Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        setParams(enchantability, accuracyModifier, damageModifier, velocityModifier);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("pull"), this::getPullModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxBowItemWrapper(stack, this);
    }

    public float getPullModelProperty(ItemStack stack, World world, LivingEntity entity) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        return MathHelper.clamp((float) (entity.getTicksUsingItem()) / 20F, 0.0F, 1.0F);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, isEmpowered(stack))) {
            return super.use(world, player, hand);
        }
        return ActionResult.fail(stack);
    }

    // region IEnergyContainerItem
    @Override
    public int getExtract(ItemStack container) {

        return extract;
    }

    @Override
    public int getReceive(ItemStack container) {

        return receive;
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {

        return getMaxStored(container, maxEnergy);
    }
    // endregion

    // region CAPABILITY WRAPPER
    protected class FluxBowItemWrapper extends EnergyContainerItemWrapper implements IArcheryBowItem {

        private final LazyOptional<IArcheryBowItem> holder = LazyOptional.of(() -> this);
        private final float accuracyModifier;
        private final float damageModifier;
        private final float velocityModifier;
        protected final int simulateTicks = 100;

        final ItemStack bowItem;

        FluxBowItemWrapper(ItemStack bowItemContainer, FluxBowItem item) {

            super(bowItemContainer, item, item.getEnergyCapability());
            this.bowItem = bowItemContainer;

            this.accuracyModifier = MathHelper.clamp(item.accuracyModifier, 0.1F, 10.0F);
            this.damageModifier = MathHelper.clamp(item.damageModifier, 0.1F, 10.0F);
            this.velocityModifier = MathHelper.clamp(item.velocityModifier, 0.1F, 10.0F);
        }

        @Override
        public float getAccuracyModifier(PlayerEntity shooter) {

            if (isEmpowered(bowItem)) {
                int duration = shooter.getTicksUsingItem();
                if (duration > 20) {
                    return Math.max(accuracyModifier * 20.0F / duration, 0.01F);
                }
            }
            return accuracyModifier;
        }

        @Override
        public float getDamageModifier(PlayerEntity shooter) {

            return damageModifier;
        }

        @Override
        public float getVelocityModifier(PlayerEntity shooter) {

            return velocityModifier;
        }

        @Override
        public void onArrowLoosed(PlayerEntity shooter) {

            useEnergy(bowItem, isEmpowered(bowItem), shooter.abilities.instabuild);
        }

        @Override
        public boolean fireArrow(ItemStack arrow, PlayerEntity shooter, int charge, World world) {

            if (isEmpowered(bowItem) && hasEnergy(bowItem, true)) {
                return fireInstantArrow(bowItem, arrow, shooter, charge, world);
            }
            return ArcheryHelper.fireArrow(bowItem, arrow, shooter, charge, world);
        }

        public boolean fireInstantArrow(ItemStack bow, ItemStack ammo, PlayerEntity shooter, int charge, World world) {

            IArcheryBowItem bowCap = bow.getCapability(BOW_ITEM_CAPABILITY).orElse(new ArcheryBowItemWrapper(bow));
            IArcheryAmmoItem ammoCap = ammo.getCapability(AMMO_ITEM_CAPABILITY).orElse(new ArcheryAmmoItemWrapper(ammo));

            boolean infinite = shooter.abilities.instabuild
                    || ammoCap.isInfinite(bow, shooter)
                    || (ArcheryHelper.isArrow(ammo) && ((ArrowItem) ammo.getItem()).isInfinite(ammo, bow, shooter))
                    || ammo.isEmpty() && getItemEnchantmentLevel(INFINITY_ARROWS, bow) > 0;

            if (!ammo.isEmpty() || infinite) {
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }
                float arrowVelocity = BowItem.getPowerForTime(charge);

                float accuracyMod = bowCap.getAccuracyModifier(shooter);
                float damageMod = bowCap.getDamageModifier(shooter);
                float velocityMod = bowCap.getVelocityModifier(shooter);

                if (arrowVelocity >= 0.1F) {
                    if (Utils.isServerWorld(world)) {
                        int encVolley = getItemEnchantmentLevel(VOLLEY, bow);
                        int encTrueshot = getItemEnchantmentLevel(TRUESHOT, bow);
                        int encPunch = getItemEnchantmentLevel(PUNCH_ARROWS, bow);
                        int encPower = getItemEnchantmentLevel(POWER_ARROWS, bow);
                        int encFlame = getItemEnchantmentLevel(FLAMING_ARROWS, bow);

                        if (encTrueshot > 0) {
                            accuracyMod *= (1.5F / (1 + encTrueshot));
                            damageMod *= (1.0F + 0.25F * encTrueshot);
                            arrowVelocity = cofh.lib.util.helpers.MathHelper.clamp(0.1F, arrowVelocity + 0.05F * encTrueshot, 1.75F);
                        }
                        int numArrows = encVolley > 0 ? 3 : 1;
                        // Each additional arrow fired at a higher arc - arrows will not be fired beyond vertically. Maximum of 5 degrees between arrows.
                        float volleyPitch = encVolley > 0 ? cofh.lib.util.helpers.MathHelper.clamp(90.0F + shooter.xRot / encVolley, 0.0F, 5.0F) : 0;

                        BowItem bowItem = bow.getItem() instanceof BowItem ? (BowItem) bow.getItem() : null;

                        for (int shot = 0; shot < numArrows; ++shot) {
                            AbstractArrowEntity arrow = ArcheryHelper.createArrow(world, ammo, shooter);
                            if (bowItem != null) {
                                arrow = bowItem.customArrow(arrow);
                            }
                            arrow.shootFromRotation(shooter, shooter.xRot - volleyPitch * shot, shooter.yRot, 0.0F, arrowVelocity * 3.0F * velocityMod, accuracyMod);// * (1 + shot * 2));
                            arrow.setBaseDamage(arrow.getBaseDamage() * damageMod);

                            if (arrowVelocity >= 1.0F) {
                                arrow.setCritArrow(true);
                            }
                            if (encTrueshot > 0) {
                                arrow.setPierceLevel((byte) encTrueshot);
                            }
                            if (encPower > 0 && arrow.getBaseDamage() > 0) {
                                arrow.setBaseDamage(arrow.getBaseDamage() + (double) encPower * 0.5D + 0.5D);
                            }
                            if (encPunch > 0) {
                                arrow.setKnockback(encPunch);
                            }
                            if (encFlame > 0) {
                                arrow.setSecondsOnFire(100);
                            }
                            if (infinite || shot > 0) {
                                arrow.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                            }
                            simulateArrow(arrow, world, simulateTicks);
                            if (arrow.isAlive()) {
                                world.addFreshEntity(arrow);
                            }
                        }
                        bowCap.onArrowLoosed(shooter);
                    }
                    world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + arrowVelocity * 0.5F);

                    if (!infinite && !shooter.abilities.instabuild) {
                        ammoCap.onArrowLoosed(shooter);
                        if (ammo.isEmpty()) {
                            shooter.inventory.removeItem(ammo);
                        }
                    }
                    shooter.awardStat(Stats.ITEM_USED.get(bow.getItem()));
                }
                return true;
            }
            return false;
        }

        public void simulateArrow(AbstractArrowEntity arrow, World world, int maxTicks) {

            for (int i = 0; i < maxTicks && arrow.isAlive() && !arrow.isOnGround() && arrow.life <= 1; ++i) {
                arrow.tick();
                Vector3d velocity = arrow.getDeltaMovement();
                if (!world.isClientSide()) {
                    Vector3d prevPos = velocity.scale(-0.5F).add(arrow.position());
                    ((ServerWorld) world).sendParticles(RedstoneParticleData.REDSTONE, prevPos.x(), prevPos.y(), prevPos.z(), 1, 0, 0, 0, 0);
                    ((ServerWorld) world).sendParticles(RedstoneParticleData.REDSTONE, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
                }
                if (velocity.lengthSqr() < 0.02F) {
                    break;
                }
            }
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == BOW_ITEM_CAPABILITY) {
                return BOW_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
