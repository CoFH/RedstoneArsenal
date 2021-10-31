package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.item.impl.ShieldItemCoFH;
import cofh.lib.util.helpers.SecurityHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.*;

public class FluxShieldItem extends ShieldItemCoFH implements IFluxItem {

    public static final double RANGE = 4;

    protected int maxEnergy;
    protected int extract;
    protected int receive;

    public FluxShieldItem(Properties builder, int maxEnergy, int maxTransfer) {

        super(builder);
        this.maxEnergy = maxEnergy;
        this.extract = maxTransfer;
        this.receive = maxTransfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("blocking"), (stack, world, entity) -> entity.isBlocking() ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && getMode(stack) > 0 ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {

        useEnergy(stack, amount * getEnergyPerUse(false), entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild);
        return 0;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        //TODO: use energy on block
        ItemStack stack = player.getItemInHand(hand);
        if (getMode(stack) > 0 && (hasEnergy(stack, true) || player.abilities.instabuild)) {
            useEnergy(stack, true, player.abilities.instabuild);
            double r2 = RANGE * RANGE;
            AxisAlignedBB searchArea = player.getBoundingBox().inflate(RANGE);
            for (Entity entity : world.getEntities(player, searchArea, EntityPredicates.NO_CREATIVE_OR_SPECTATOR)) {
                if (player.distanceToSqr(entity) < r2) {
                    Vector3d push = entity.getDeltaMovement().lengthSqr() < 1.0 ? entity.position().subtract(player.position()).normalize() : entity.getDeltaMovement().reverse();
                    entity.setDeltaMovement(push);
                }
            }
        }
        return super.use(world, player, hand);
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {

        return stack.getItem() instanceof ShieldItem;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {

        return false;
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
