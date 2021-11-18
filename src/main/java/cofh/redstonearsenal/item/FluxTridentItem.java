package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.item.impl.TridentItemCoFH;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.entity.FluxTridentEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static cofh.lib.util.constants.Constants.UUID_ENCH_REACH_DISTANCE;
import static cofh.lib.util.constants.Constants.UUID_TOOL_REACH;
import static cofh.lib.util.references.CoreReferences.LIGHTNING_RESISTANCE;

public class FluxTridentItem extends TridentItemCoFH implements IFluxItem {

    public static final double PLUNGE_RANGE = 3.5;
    public static final double PLUNGE_SPEED = 3;

    protected final float damage;
    protected final float attackSpeed;
    protected final float addedReach;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxTridentItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, float reachIn, Properties builder, int energy, int xfer) {

        super(tier, builder);

        this.damage = attackDamageIn + tier.getAttackDamageBonus();
        this.attackSpeed = attackSpeedIn;
        this.addedReach = reachIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("throwing"), (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    public FluxTridentItem(IItemTier tier, int enchantability, int attackDamageIn, float attackSpeedIn, float reachIn, Properties builder, int energy, int xfer) {

        this(tier, attackDamageIn, attackSpeedIn, reachIn, builder, energy, xfer);

        this.enchantability = enchantability;
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
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {

        super.inventoryTick(stack, world, entity, itemSlot, isSelected);

        if (entity.isOnGround() && entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (living.isAutoSpinAttack()) {
                stopSpinAttack(living);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false) && !player.isAutoSpinAttack() && (EnchantmentHelper.getRiptide(stack) <= 0 || player.isInWaterOrRain())) {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }
        return ActionResult.fail(stack);
    }

    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int remainingDuration) {

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            int i = this.getUseDuration(stack) - remainingDuration;
            if (i >= 10) {
                int riptideLevel = EnchantmentHelper.getRiptide(stack);
                if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                    if (!world.isClientSide && useEnergy(stack, false, player.abilities.instabuild)) {
                        if (riptideLevel == 0) {
                            FluxTridentEntity tridentEntity = new FluxTridentEntity(world, player, stack);
                            tridentEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F + riptideLevel * 0.5F, 1.0F);
                            if (player.abilities.instabuild) {
                                tridentEntity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                            }

                            world.addFreshEntity(tridentEntity);
                            world.playSound(null, tridentEntity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            if (!player.abilities.instabuild) {
                                player.inventory.removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptideLevel > 0) {
                        float degToRad = (float)Math.PI / 180F;
                        float xRotRad = player.xRot * degToRad;
                        float yRotRad = player.yRot * degToRad;
                        float xPush = -MathHelper.sin(yRotRad) * MathHelper.cos(xRotRad);
                        float yPush = -MathHelper.sin(xRotRad);
                        float zPush = MathHelper.cos(yRotRad) * MathHelper.cos(xRotRad);
                        float riptideMult = (1.0F + riptideLevel) * 0.75F / MathHelper.sqrt(xPush * xPush + yPush * yPush + zPush * zPush);
                        player.push(xPush * riptideMult, yPush * riptideMult, zPush * riptideMult);
                        player.startAutoSpinAttack(20);
                        if (player.isOnGround()) {
                            player.move(MoverType.SELF, new Vector3d(0.0D, 1.1999999, 0.0D));
                        }

                        SoundEvent soundevent;
                        if (riptideLevel >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (riptideLevel == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }
                        world.playSound(null, player, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    public boolean startPlunge(LivingEntity living) {

        if (!canStartPlunging(living)) {
            return false;
        }
        living.startAutoSpinAttack(200);
        Vector3d motion = getPlungeVector(living.getLookAngle(), PLUNGE_SPEED);
        living.push(motion.x(), motion.y(), motion.z());
        return true;
    }

    public static boolean canStartPlunging(LivingEntity living) {

        if (living.isOnGround() || living.isAutoSpinAttack()) {
            return false;
        }
        RayTraceContext context = new RayTraceContext(living.position(), living.position().add(0, -3, 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, living);
        return living.level.clip(context).getType().equals(RayTraceResult.Type.MISS);
    }

    public boolean plungeAttack(World world, LivingEntity attacker, ItemStack stack) {

        if (attacker.fallDistance <= attacker.getMaxFallDistance() || !isEmpowered(stack) || !useEnergy(stack, true, attacker instanceof PlayerEntity && ((PlayerEntity) attacker).abilities.instabuild)) {
            return false;
        }
        if (Utils.getItemEnchantmentLevel(Enchantments.CHANNELING, stack) > 0) {
            if (world.canSeeSky(attacker.blockPosition()) && world instanceof ServerWorld && world.isThundering()) {
                attacker.addEffect(new EffectInstance(LIGHTNING_RESISTANCE, 40, 1, false, false));
                Utils.spawnLightningBolt(world, attacker.blockPosition(), attacker);
            }
        }
        double r2 = PLUNGE_RANGE * PLUNGE_RANGE;
        AxisAlignedBB searchArea = attacker.getBoundingBox().inflate(PLUNGE_RANGE, 1, PLUNGE_RANGE);
        Predicate<Entity> filter = EntityPredicates.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof LivingEntity);
        boolean hit = false;
        for (Entity target : world.getEntities(attacker, searchArea, filter)) {
            if (attacker.distanceToSqr(target) < r2) {
                hit |= target.hurt(IFluxItem.fluxDirectDamage(attacker), getPlungeAttackDamage(attacker, stack));
            }
        }
        return hit;
    }

    public static void stopSpinAttack(LivingEntity living) {

        if (living.isAutoSpinAttack()) {
            living.autoSpinAttackTicks = 0;
            AxisAlignedBB noVolume = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
            living.checkAutoSpinAttack(noVolume, noVolume);
        }
    }

    public static Vector3d getPlungeVector(Vector3d lookVector, double magnitude) {

        double x = lookVector.x();
        double y = lookVector.y();
        double z = lookVector.z();
        double compSqr = lookVector.lengthSqr() * 0.5;
        if (compSqr > y * y || y > 0) {
            double comp = MathHelper.sqrt(compSqr);
            double horzSum = Math.abs(x) + Math.abs(z);
            return new Vector3d((x / horzSum) * comp, -comp, (z / horzSum) * comp).scale(magnitude);
        }
        return new Vector3d(x, y, z).scale(magnitude);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        useEnergy(stack, false, ((PlayerEntity) attacker).abilities.instabuild);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving instanceof PlayerEntity && ((PlayerEntity) entityLiving).abilities.instabuild);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(UUID_TOOL_REACH, "Weapon modifier", getAddedReach(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? damage : 0.0F;
    }

    protected float getPlungeAttackDamage(LivingEntity living, ItemStack stack) {

        return hasEnergy(stack, true) && living.fallDistance > living.getMaxFallDistance() ? 2.5F * MathHelper.sqrt(living.fallDistance) : 0.0F;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return attackSpeed;
    }

    protected float getAddedReach(ItemStack stack) {

        return addedReach;
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
}
