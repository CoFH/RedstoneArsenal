package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.impl.SwordItemCoFH;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.entity.FluxSlashEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class FluxSwordItem extends SwordItemCoFH implements IFluxItem {

    protected final float damage;
    protected final float damageCharged;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxSwordItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        this.damage = getDamage();
        this.damageCharged = damage + 4.0F;
        this.attackSpeed = attackSpeedIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && getMode(stack) > 0 ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            if (!player.abilities.instabuild && hasEnergy(stack)) {
                useEnergy(stack, false);
            }

            if (getMode(stack) > 0) {
                if (canSweepAttack(player)) { //TODO: player attack strength already reset once this is called. time to use Mick's Inn?
                    shootFluxSlash(stack, player);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {

        if (getMode(stack) > 0 && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;

            if (canSweepAttack(player)) {
                shootFluxSlash(stack, player);
            }
        }

        return false;
    }

    protected void shootFluxSlash(ItemStack stack, PlayerEntity player) {

        if (hasEnergy(stack, true) || !player.abilities.instabuild) {
            useEnergy(stack, true, player.abilities.instabuild);

            World world = player.level;
            if (!world.isClientSide()) {
                FluxSlashEntity projectile = new FluxSlashEntity(world, player);
                world.addFreshEntity(projectile);
            }
        }
    }

    public static boolean canSweepAttack(PlayerEntity player) {

        // &&(player.isOnGround() || player.onClimbable() || player.isInWater() || player.hasEffect(Effects.BLINDNESS) || player.isPassenger())
        return player.getAttackStrengthScale(0.5F) > 0.9F && !player.isSprinting();
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            if (entityLiving instanceof PlayerEntity && !((PlayerEntity) entityLiving).abilities.instabuild) {
                useEnergy(stack, false);
            }
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack) ? getMode(stack) > 0 ? damageCharged : damage : 0.0F;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return hasEnergy(stack) && getMode(stack) > 0 ? attackSpeed + 0.2F : attackSpeed;
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
