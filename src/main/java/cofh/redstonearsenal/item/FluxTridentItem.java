package cofh.redstonearsenal.item;

import cofh.core.client.particle.options.CylindricalParticleOptions;
import cofh.core.config.CoreClientConfig;
import cofh.core.init.CoreParticles;
import cofh.core.item.ILeftClickHandlerItem;
import cofh.core.util.ProxyUtils;
import cofh.lib.item.TridentItemCoFH;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.MathHelper;
import cofh.redstonearsenal.client.renderer.FluxTridentBEWLR;
import cofh.redstonearsenal.entity.ThrownFluxTrident;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static cofh.core.init.CoreMobEffects.LIGHTNING_RESISTANCE;
import static cofh.core.init.CoreParticles.BLAST_WAVE;
import static cofh.lib.util.Constants.UUID_WEAPON_RANGE;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxTridentItem extends TridentItemCoFH implements IMultiModeFluxItem, ILeftClickHandlerItem {

    public static final double PLUNGE_RANGE = 2.0;
    public static final double PLUNGE_SPEED = 3;

    protected final float damage;
    protected final float attackSpeed;
    protected final float addedRange;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxTridentItem(Tier tier, int attackDamageIn, float attackSpeedIn, float rangeIn, Properties builder, int energy, int xfer) {

        super(tier, builder);

        this.damage = attackDamageIn + tier.getAttackDamageBonus();
        this.attackSpeed = attackSpeedIn;
        this.addedRange = rangeIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("throwing"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().equals(stack) ? 1.0F : 0.0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    public FluxTridentItem(Tier tier, int enchantability, int attackDamageIn, float attackSpeedIn, float reachIn, Properties builder, int energy, int xfer) {

        this(tier, attackDamageIn, attackSpeedIn, reachIn, builder, energy, xfer);

        this.enchantability = enchantability;
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails.get()) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getEnchantmentValue(stack) > 0;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {

        super.inventoryTick(stack, world, entity, itemSlot, isSelected);

        if (entity instanceof LivingEntity living) {
            if (living.isAutoSpinAttack() && (living.isOnGround() || (living.isUnderWater() && living.getDeltaMovement().lengthSqr() < 0.09F))) {
                stopSpinAttack(living);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false) && !player.isAutoSpinAttack() && (EnchantmentHelper.getRiptide(stack) <= 0 || player.isInWaterOrRain())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int remainingDuration) {

        if (entity instanceof Player player) {
            int i = this.getUseDuration(stack) - remainingDuration;
            if (i >= 10) {
                int riptideLevel = EnchantmentHelper.getRiptide(stack);
                if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                    if (!world.isClientSide && useEnergy(stack, false, player.abilities.instabuild)) {
                        if (riptideLevel == 0) {
                            ThrownFluxTrident tridentEntity = new ThrownFluxTrident(world, player, stack);
                            tridentEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F + riptideLevel * 0.5F, 1.0F);
                            if (player.abilities.instabuild) {
                                tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }
                            world.addFreshEntity(tridentEntity);
                            world.playSound(null, tridentEntity, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.abilities.instabuild) {
                                player.inventory.removeItem(stack);
                            }
                        }
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptideLevel > 0) {
                        float degToRad = (float) Math.PI / 180F;
                        float xRotRad = player.xRot * degToRad;
                        float yRotRad = player.yRot * degToRad;
                        float xPush = -MathHelper.sin(yRotRad) * MathHelper.cos(xRotRad);
                        float yPush = -MathHelper.sin(xRotRad);
                        float zPush = MathHelper.cos(yRotRad) * MathHelper.cos(xRotRad);
                        float riptideMult = (1.0F + riptideLevel) * 0.75F / MathHelper.sqrt(xPush * xPush + yPush * yPush + zPush * zPush);
                        player.push(xPush * riptideMult, yPush * riptideMult, zPush * riptideMult);
                        player.startAutoSpinAttack(20);
                        if (player.isOnGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999, 0.0D));
                        }
                        SoundEvent soundevent;
                        if (riptideLevel >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (riptideLevel == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }
                        world.playSound(null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {

        // Counteract riptide "bounce"
        if (player.isAutoSpinAttack() && player.fallDistance > 3) {
            player.fallDistance = 0;
            player.setDeltaMovement(player.getDeltaMovement().scale(-5.0D));
        }
        return false;
    }

    public boolean startPlunge(LivingEntity living) {

        if (!canStartPlunging(living)) {
            return false;
        }
        if (living instanceof Player player) {
            player.stopFallFlying();
            player.abilities.flying = false;
            player.startAutoSpinAttack(200);
        }
        Vec3 motion = getPlungeVector(living.getLookAngle(), getPlungeSpeed());
        living.push(motion.x(), motion.y(), motion.z());
        return true;
    }

    public static boolean canStartPlunging(LivingEntity living) {

        if (living.isOnGround() || living.isAutoSpinAttack()) {
            return false;
        }
        ClipContext context = new ClipContext(living.position(), living.position().add(0, -3, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living);
        return living.level.clip(context).getType().equals(HitResult.Type.MISS);
    }

    public boolean plungeAttack(Level world, LivingEntity attacker, ItemStack stack) {

        if (attacker.fallDistance <= attacker.getMaxFallDistance() || !isEmpowered(stack) || !useEnergy(stack, true, attacker)) {
            return false;
        }
        float range = getPlungeRange(attacker.fallDistance);
        if (world.isClientSide) {
            //TODO circle particle
            world.addParticle(new CylindricalParticleOptions(CoreParticles.BLAST_WAVE.get(), range * 2.0F, range * 3.0F, 1.5F), attacker.getX(), attacker.getY(), attacker.getZ(), 0, 0, 0);
            return true;
        }
        if (Utils.getItemEnchantmentLevel(Enchantments.CHANNELING, stack) > 0) {
            if (world.canSeeSky(attacker.blockPosition()) && world instanceof ServerLevel && world.isThundering()) {
                attacker.addEffect(new MobEffectInstance(LIGHTNING_RESISTANCE.get(), 40, 0, false, false));
                Utils.spawnLightningBolt(world, attacker.blockPosition(), attacker);
            }
        }
        double r2 = range * range;
        boolean hit = false;
        for (Entity target : world.getEntities(attacker, attacker.getBoundingBox().inflate(range, 1, range), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (attacker.distanceToSqr(target) <= r2) {
                hit |= target.hurt(IFluxItem.fluxDirectDamage(attacker), getPlungeAttackDamage(attacker, stack));
            }
        }
        if (hit) {
            world.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 10.0F, 1.0F);
        } else {
            world.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 3.0F, 1.0F);
        }
        return hit;
    }

    public static void stopSpinAttack(LivingEntity living) {

        if (living.isAutoSpinAttack()) {
            living.autoSpinAttackTicks = 0;
            AABB noVolume = new AABB(0, 0, 0, 0, 0, 0);
            living.checkAutoSpinAttack(noVolume, noVolume);
        }
    }

    public static Vec3 getPlungeVector(Vec3 lookVector, double magnitude) {

        double x = lookVector.x();
        double y = lookVector.y();
        double z = lookVector.z();
        double compSqr = lookVector.lengthSqr() * 0.75;
        if (x < 0.0001F && z < 0.0001F) {
            return new Vec3(0, -magnitude, 0);
        }
        if (compSqr > y * y || y > 0) {
            double comp = Math.sqrt(compSqr);
            double horzSum = Math.abs(x) + Math.abs(z);
            return new Vec3((x / horzSum) * comp, -comp, (z / horzSum) * comp).scale(magnitude);
        }
        return lookVector.scale(magnitude);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        useEnergy(stack, false, attacker);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(UUID_WEAPON_RANGE, "Weapon modifier", getAddedAttackRange(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? damage : 0.0F;
    }

    protected float getPlungeAttackDamage(LivingEntity living, ItemStack stack) {

        return hasEnergy(stack, true) && living.fallDistance > living.getMaxFallDistance() ? 2.5F * MathHelper.sqrt(living.fallDistance) : 0.0F;
    }

    public float getPlungeRange(float height) {

        return -20.0F / (7.0F + height) + 4.5F;
    }

    public double getPlungeSpeed() {

        return PLUNGE_SPEED;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return attackSpeed;
    }

    protected float getAddedAttackRange(ItemStack stack) {

        return addedRange;
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

    // region CLIENT
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {

                return FluxTridentBEWLR.INSTANCE;
            }
        });
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

    // region ILeftClickHandlerItem
    public void onLeftClick(Player player, ItemStack stack) {

        if (isEmpowered(stack) && hasEnergy(stack, true)) {
            startPlunge(player);
        }
    }
    // endregion
}
