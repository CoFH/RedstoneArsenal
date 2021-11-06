package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.capability.IArcheryAmmoItem;
import cofh.lib.capability.IArcheryBowItem;
import cofh.lib.capability.templates.ArcheryAmmoItemWrapper;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.ArcheryHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cofh.lib.capability.CapabilityArchery.AMMO_ITEM_CAPABILITY;
import static cofh.lib.capability.CapabilityArchery.BOW_ITEM_CAPABILITY;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxCrossbowItem extends CrossbowItem implements IFluxItem {

    protected static final float[] REPEAT_DURATIONS = getRepeatDurations(1.4F, 0.6F, -0.2F, 20);

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    protected float accuracyModifier;
    protected float damageModifier;
    protected float velocityModifier;

    protected int repeats = 1;

    public FluxCrossbowItem(float accuracyModifier, float damageModifier, float velocityModifier, Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        this.accuracyModifier = accuracyModifier;
        this.damageModifier = damageModifier;
        this.velocityModifier = velocityModifier;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("pull"), this::getPullProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("firework"), (stack, world, entity) -> !getLoadedProjectile(stack).isEmpty() && (getLoadedProjectile(stack).getItem() instanceof FireworkRocketItem) ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("arrow"), (stack, world, entity) -> !getLoadedProjectile(stack).isEmpty() && !(getLoadedProjectile(stack).getItem() instanceof FireworkRocketItem) ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
        List<ITextComponent> additionalTooltips = new ArrayList<>();
        super.appendHoverText(stack, worldIn, additionalTooltips, flagIn);

        if (!additionalTooltips.isEmpty()) {
            if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
                tooltip.addAll(additionalTooltips);
            }
            else if (CoreConfig.holdShiftForDetails) {
                tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
            }
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxCrossbowItemWrapper(stack, accuracyModifier, damageModifier, velocityModifier);
    }

    public float getPullProperty(ItemStack stack, World world, LivingEntity entity) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        int baseDuration = getUseDuration(stack);
        int duration = baseDuration - entity.getUseItemRemainingTicks();

        if (isEmpowered(stack)) {
            if (repeats >= REPEAT_DURATIONS.length) {
                return 0.0F;
            }
            int next = MathHelper.floor(REPEAT_DURATIONS[repeats] * getUseDuration(stack));
            int prev = MathHelper.floor(REPEAT_DURATIONS[repeats - 1] * getUseDuration(stack));

            return MathHelper.clamp(((float) duration - prev) / (next - prev), 0.0F, 1.0F);
        }
        else {
            return MathHelper.clamp((float) (duration) / baseDuration, 0.0F, 1.0F);
        }
    }

    public static float[] getRepeatDurations(float start, float end, float change, int repeats) {

        float[] durations = new float[repeats + 1];
        float duration = 0;
        for (int i = 0; i < durations.length; ++i) {
            durations[i] = duration;
            duration += start;
            start = Math.max(start + change, end);
        }
        return durations;
    }

    @Override
    public int getUseDuration(ItemStack stack) {

        return getChargeDuration(stack);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false)) {
            if (!isEmpowered(stack) && isCharged(stack)) {
                setCharged(stack, !shoot(world, player, stack));
            }
            else if (!getAmmo(player, stack).isEmpty()) {
                repeats = 1;
                player.startUsingItem(hand);
            }
            return ActionResult.consume(stack);
        }
        return ActionResult.pass(stack);
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int durationRemaining) {

        if (!world.isClientSide() && hasEnergy(stack, false)) {
            int baseDuration = getUseDuration(stack);
            int duration = baseDuration - durationRemaining;

            if (isEmpowered(stack)) {
                if (repeats >= REPEAT_DURATIONS.length) {
                    return;
                }

                int next = MathHelper.floor(REPEAT_DURATIONS[repeats] * getUseDuration(stack));
                if (repeats > 0) {
                    if (duration == next - 2) {
                        if (!loadAmmo(living, stack)) {
                            repeats = REPEAT_DURATIONS.length;
                            living.releaseUsingItem();
                            return;
                        }
                    }
                    else if (duration == next) {
                        if (!world.isClientSide()) {
                            ++repeats;
                        }
                        if (useEnergy(stack, true, living instanceof PlayerEntity && ((PlayerEntity) living).abilities.instabuild)) {
                            shoot(world, living, stack);
                        }
                        else {
                            living.releaseUsingItem();
                            return;
                        }
                        if (repeats >= REPEAT_DURATIONS.length) {
                            living.releaseUsingItem();
                        }
                        return;
                    }
                }

                int prev = MathHelper.floor(REPEAT_DURATIONS[repeats - 1] * getUseDuration(stack));
                duration -= prev;
                baseDuration = next - prev;
            }

            if (duration == baseDuration / 4) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), getStartSound(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack)), SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
            if (duration == baseDuration / 2) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int durationRemaining) {

        if (!isEmpowered(stack) && durationRemaining < 0 && !isCharged(stack) && loadAmmo(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        else if (isEmpowered(stack) && living instanceof PlayerEntity) {
            int duration = getUseDuration(stack) - durationRemaining;
            if (duration >= MathHelper.floor(REPEAT_DURATIONS[1] * getUseDuration(stack))) {
                ((PlayerEntity) living).getCooldowns().addCooldown(this, Math.min(duration, 200));
            }
        }
    }

    public boolean shoot(World world, LivingEntity living, ItemStack stack) {

        if (living instanceof PlayerEntity) {
            LazyOptional<IArcheryBowItem> cap = stack.getCapability(BOW_ITEM_CAPABILITY);
            ItemStack ammo = getLoadedProjectile(stack);
            if (cap.isPresent() && !ammo.isEmpty()) {
                return cap.resolve().get().fireArrow(ammo, (PlayerEntity) living, 1, world);
            }
        }
        return false;
    }

    public static ItemStack getAmmo(LivingEntity living, ItemStack crossbow) {

        ItemStack ammo = ItemStack.EMPTY;
        ItemStack offHand = living.getOffhandItem();
        ItemStack mainHand = living.getMainHandItem();
        Predicate<ItemStack> isAmmo  = ((ShootableItem) crossbow.getItem()).getSupportedHeldProjectiles();

        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            if (offHand.getCapability(AMMO_ITEM_CAPABILITY).map(cap -> !cap.isEmpty(player)).orElse(false) || isAmmo.test(offHand)) {
                return offHand;
            }
            if (mainHand.getCapability(AMMO_ITEM_CAPABILITY).map(cap -> !cap.isEmpty(player)).orElse(false) || isAmmo.test(mainHand)) {
                return mainHand;
            }
        }
        if (isAmmo.test(offHand)) {
            return offHand;
        }
        if (isAmmo.test(mainHand)) {
            return mainHand;
        }
        if (living instanceof PlayerEntity) {
            ammo = ArcheryHelper.findAmmo((PlayerEntity) living);
        }
        if (ammo.isEmpty()) {
            ammo = living.getProjectile(crossbow);
        }
        return ammo;
    }

    public static boolean loadAmmo(LivingEntity living, ItemStack crossbow) {

        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            ItemStack ammo = getAmmo(living, crossbow);
            if (!ammo.isEmpty() && ammo.getItem() instanceof FireworkRocketItem) {
                crossbow.getOrCreateTag().put("projectile", ammo.save(new CompoundNBT()));
                ammo.shrink(1);
                setCharged(crossbow, true);
                return true;
            }
            IArcheryAmmoItem ammoCap = ammo.getCapability(AMMO_ITEM_CAPABILITY).orElse(new ArcheryAmmoItemWrapper(ammo));
            boolean infinite = player.abilities.instabuild
                    || ammoCap.isInfinite(crossbow, player)
                    || (ArcheryHelper.isArrow(ammo) && ((ArrowItem) ammo.getItem()).isInfinite(ammo, crossbow, player));
            if (!ammo.isEmpty() || infinite) {
                crossbow.getOrCreateTag().put("projectile", ammo.save(new CompoundNBT()));
                setCharged(crossbow, true);
                if (!infinite) {
                    ammoCap.onArrowLoosed(player);
                    if (ammo.isEmpty()) {
                        player.inventory.removeItem(ammo);
                    }
                }
                return true;
            }
        }
        ItemStack ammo = living.getProjectile(crossbow);
        if (!ammo.isEmpty()) {
            crossbow.getOrCreateTag().put("projectile", ammo.save(new CompoundNBT()));
            setCharged(crossbow, true);
            return true;
        }

        return false;
    }

    public ItemStack getLoadedProjectile(ItemStack crossbow) {

        CompoundNBT nbt = crossbow.getTag();
        if (nbt != null && nbt.contains("projectile")) {
            return ItemStack.of(nbt.getCompound("projectile"));
        }
        return ItemStack.EMPTY;
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

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        if (player.getUseItemRemainingTicks() < 0) {
            setMode(stack, isEmpowered(stack) ? 0 : 1);
            player.releaseUsingItem();
            setMode(stack, isEmpowered(stack) ? 0 : 1);
        }
        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
            setCharged(stack, true);
        }
        else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
            setCharged(stack, !getLoadedProjectile(stack).isEmpty());
        }
    }

    // region CAPABILITY WRAPPER
    protected class FluxCrossbowItemWrapper extends EnergyContainerItemWrapper implements IArcheryBowItem {

        private final LazyOptional<IArcheryBowItem> holder = LazyOptional.of(() -> this);
        private final float accuracyModifier;
        private final float damageModifier;
        private final float velocityModifier;

        final ItemStack crossbowItem;

        FluxCrossbowItemWrapper(ItemStack bowItemContainer, float accuracyModifier, float damageModifier, float velocityModifier) {

            super(bowItemContainer, (IEnergyContainerItem) bowItemContainer.getItem());
            this.crossbowItem = bowItemContainer;

            this.accuracyModifier = MathHelper.clamp(accuracyModifier, 0.1F, 10.0F);
            this.damageModifier = MathHelper.clamp(damageModifier, 0.1F, 10.0F);
            this.velocityModifier = MathHelper.clamp(velocityModifier, 0.1F, 10.0F);
        }

        FluxCrossbowItemWrapper(ItemStack bowItemContainer) {

            this(bowItemContainer, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public float getAccuracyModifier(PlayerEntity shooter) {

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

        }

        @Override
        public boolean fireArrow(ItemStack ammo, PlayerEntity shooter, int charge, World world) {

            int multishot = Utils.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbowItem);
            int damage = 0;
            for(int i = -multishot; i <= multishot; ++i) {
                if (!ammo.isEmpty()) {
                    ProjectileEntity projectile;
                    if (ammo.getCapability(AMMO_ITEM_CAPABILITY).isPresent() || ammo.getItem() instanceof ArrowItem) {
                        AbstractArrowEntity arrow = ArcheryHelper.createArrow(world, ammo, shooter);
                        projectile = adjustArrow(arrow, shooter.abilities.instabuild || i != 0);
                        ++damage;
                    }
                    else if (ammo.getItem() instanceof FireworkRocketItem) {
                        projectile = new FireworkRocketEntity(world, ammo, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15F, shooter.getZ(), true);
                        damage += 3;
                    }
                    else {
                        return false;
                    }

                    shootProjectile(shooter, projectile, getBaseSpeed(ammo) * getVelocityModifier(shooter), getAccuracyModifier(shooter), i * 10.F);
                    world.addFreshEntity(projectile);
                    float pitch = 1.0F / (random.nextFloat() * 0.5F + 1.8F) + (shooter.getRandom().nextBoolean() ? 0.63F : 0.43F);
                    world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, pitch);
                }
            }

            crossbowItem.removeTagKey("projectile");
            useEnergy(crossbowItem, Math.min(ENERGY_PER_USE_EMPOWERED * damage, getEnergyStored()), shooter.abilities.instabuild);

            if (shooter instanceof ServerPlayerEntity) {
                if (!world.isClientSide) {
                    CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayerEntity) shooter, crossbowItem);
                }
                shooter.awardStat(Stats.ITEM_USED.get(crossbowItem.getItem()));
            }
            return true;
        }

        public float getBaseSpeed(ItemStack ammo) {

            return ammo.getItem() instanceof FireworkRocketItem ? 1.6F : 3.15F;
        }

        public ProjectileEntity shootProjectile(PlayerEntity shooter, ProjectileEntity projectile, float speed, float inaccuracy, float angle) {

            Vector3f vector3f = new Vector3f(shooter.getViewVector(1.0F));
            vector3f.transform(new Quaternion(new Vector3f(shooter.getUpVector(1.0F)), angle, true));
            projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, inaccuracy);
            return projectile;
        }

        public AbstractArrowEntity adjustArrow(AbstractArrowEntity arrow, boolean creativePickup) {

            arrow.setCritArrow(true);
            arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
            arrow.setShotFromCrossbow(true);
            int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbowItem);
            if (pierce > 0) {
                arrow.setPierceLevel((byte) pierce);
            }
            if (creativePickup) {
                arrow.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
            }
            return arrow;
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
