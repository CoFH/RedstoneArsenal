package cofh.redstonearsenal.item;

import cofh.core.capability.templates.ArcheryAmmoItemWrapper;
import cofh.core.capability.templates.ArcheryBowItemWrapper;
import cofh.core.config.CoreClientConfig;
import cofh.core.item.BowItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ArcheryHelper;
import cofh.lib.api.capability.IArcheryAmmoItem;
import cofh.lib.api.capability.IArcheryBowItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.capability.CapabilityArchery.AMMO_ITEM_CAPABILITY;
import static cofh.core.capability.CapabilityArchery.BOW_ITEM_CAPABILITY;
import static cofh.core.util.references.EnsorcIDs.ID_TRUESHOT;
import static cofh.core.util.references.EnsorcIDs.ID_VOLLEY;
import static cofh.lib.util.Utils.getEnchantment;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.ModIds.ID_ENSORCELLATION;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.world.item.enchantment.Enchantments.*;

public class FluxBowItem extends BowItemCoFH implements IMultiModeFluxItem {

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
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override

    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails.get()) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new FluxBowItemWrapper(stack, this);
    }

    public float getPullModelProperty(ItemStack stack, Level world, LivingEntity entity, int seed) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        return MathHelper.clamp((float) (entity.getTicksUsingItem()) / 20F, 0.0F, 1.0F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, isEmpowered(stack))) {
            return super.use(world, player, hand);
        }
        return InteractionResultHolder.fail(stack);
    }

    // region DURABILITY BAR
    @Override
    public boolean isBarVisible(ItemStack stack) {

        return IMultiModeFluxItem.super.isBarVisible(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {

        return IMultiModeFluxItem.super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {

        return IMultiModeFluxItem.super.getBarWidth(stack);
    }
    // endregion

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
        public float getAccuracyModifier(Player shooter) {

            return accuracyModifier;
        }

        @Override
        public float getDamageModifier(Player shooter) {

            return damageModifier;
        }

        @Override
        public float getVelocityModifier(Player shooter) {

            return velocityModifier;
        }

        @Override
        public void onArrowLoosed(Player shooter) {

            useEnergy(bowItem, isEmpowered(bowItem), shooter.abilities.instabuild);
        }

        @Override
        public boolean fireArrow(ItemStack arrow, Player shooter, int charge, Level world) {

            if (isEmpowered(bowItem) && hasEnergy(bowItem, true)) {
                return fireInstantArrow(bowItem, arrow, shooter, charge, world);
            }
            return ArcheryHelper.fireArrow(bowItem, arrow, shooter, charge, world);
        }

        public boolean fireInstantArrow(ItemStack bow, ItemStack ammo, Player shooter, int charge, Level world) {

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
                        int encVolley = getItemEnchantmentLevel(getEnchantment(ID_ENSORCELLATION, ID_VOLLEY), bow);
                        int encTrueshot = getItemEnchantmentLevel(getEnchantment(ID_ENSORCELLATION, ID_TRUESHOT), bow);
                        int encPunch = getItemEnchantmentLevel(PUNCH_ARROWS, bow);
                        int encPower = getItemEnchantmentLevel(POWER_ARROWS, bow);
                        int encFlame = getItemEnchantmentLevel(FLAMING_ARROWS, bow);

                        if (encTrueshot > 0) {
                            accuracyMod *= (1.5F / (1 + encTrueshot));
                            damageMod *= (1.0F + 0.25F * encTrueshot);
                            arrowVelocity = MathHelper.clamp(0.1F, arrowVelocity + 0.05F * encTrueshot, 1.75F);
                        }
                        int numArrows = encVolley > 0 ? 3 : 1;
                        // Each additional arrow fired at a higher arc - arrows will not be fired beyond vertically. Maximum of 5 degrees between arrows.
                        float volleyPitch = encVolley > 0 ? MathHelper.clamp(90.0F + shooter.xRot / encVolley, 0.0F, 5.0F) : 0;

                        BowItem bowItem = bow.getItem() instanceof BowItem ? (BowItem) bow.getItem() : null;

                        for (int shot = 0; shot < numArrows; ++shot) {
                            AbstractArrow arrow = ArcheryHelper.createArrow(world, ammo, shooter);
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
                                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }
                            simulateArrow(arrow, world, simulateTicks);
                            if (arrow.isAlive()) {
                                world.addFreshEntity(arrow);
                            }
                        }
                        bowCap.onArrowLoosed(shooter);
                    }
                    world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + arrowVelocity * 0.5F);

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

        public void simulateArrow(AbstractArrow arrow, Level world, int maxTicks) {

            for (int i = 0; i < maxTicks && arrow.isAlive() && !arrow.isOnGround() && arrow.life <= 1; ++i) {
                arrow.tick();
                Vec3 velocity = arrow.getDeltaMovement();
                if (!world.isClientSide()) {
                    Vec3 prevPos = velocity.scale(-0.5F).add(arrow.position());
                    ((ServerLevel) world).sendParticles(DustParticleOptions.REDSTONE, prevPos.x(), prevPos.y(), prevPos.z(), 1, 0, 0, 0, 0);
                    ((ServerLevel) world).sendParticles(DustParticleOptions.REDSTONE, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
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
