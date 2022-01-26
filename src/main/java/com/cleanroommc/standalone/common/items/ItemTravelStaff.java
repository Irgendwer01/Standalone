package com.cleanroommc.standalone.common.items;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneItem;
import com.cleanroommc.standalone.api.teleport.ITravelItem;
import com.cleanroommc.standalone.api.teleport.TravelController;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.common.StandaloneConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemTravelStaff extends StandaloneItem implements ITravelItem {

    private long lastBlinkTick = 0;
    private final int maxEnergyStorage;
    private int energyStored = 100_000;

    public ItemTravelStaff(int maxEnergyStorage) {
        super(new ItemSettings().creativeTab(CreativeTabs.TOOLS).maxCount(1).translationKey("travel_staff"));
        this.maxEnergyStorage = maxEnergyStorage;
        setHasSubtypes(true);
    }

    @Override
    public @Nonnull
    ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack equipped = player.getHeldItem(hand);
        if (player.isSneaking()) {
            long ticksSinceBlink = Standalone.proxy.getTickCount() - lastBlinkTick;
            if (ticksSinceBlink < 0) {
                lastBlinkTick = -1;
            }
            if (StandaloneConfig.travel.enableBlink && world.isRemote && ticksSinceBlink >= StandaloneConfig.travel.blinkDelay) {
                if (TravelController.doBlink(equipped, hand, player)) {
                    player.swingArm(hand);
                    lastBlinkTick = Standalone.proxy.getTickCount();
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, equipped);
        }

        if (world.isRemote)
            TravelController.activateTravelAccessible(equipped, hand, world, player, TravelSource.STAFF);

        player.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, equipped);
    }

    @Override
    public boolean canDestroyBlockInCreative(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack stack, @Nonnull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isActive(@Nonnull EntityPlayer ep, @Nonnull ItemStack equipped) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public int getEnergyStored(@Nonnull ItemStack item) {
        return this.energyStored;
    }

    @Override
    public void extractInternal(@Nonnull ItemStack item, int power) {
        this.energyStored = Math.max(0, this.energyStored - power);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(energyStored + " / " + maxEnergyStorage);
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        return 1.0D * (maxEnergyStorage - this.energyStored) / maxEnergyStorage;
    }
}
