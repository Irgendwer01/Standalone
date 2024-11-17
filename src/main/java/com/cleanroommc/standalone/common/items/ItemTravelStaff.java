package com.cleanroommc.standalone.common.items;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneItem;
import com.cleanroommc.standalone.api.capabilities.Capabilities;
import com.cleanroommc.standalone.api.energy.ItemEnergyStorage;
import com.cleanroommc.standalone.api.teleport.ITravelItem;
import com.cleanroommc.standalone.api.teleport.TravelController;
import com.cleanroommc.standalone.api.teleport.TravelSource;
import com.cleanroommc.standalone.common.StandaloneConfig;
import gregtech.api.capability.IElectricItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class ItemTravelStaff extends StandaloneItem implements ITravelItem {

    private long lastBlinkTick = 0;
    private final int maxEnergyStorage;

    public ItemTravelStaff(int maxEnergyStorage) {
        super(new ItemSettings().creativeTab(CreativeTabs.TOOLS).maxCount(1).translationKey("travel_staff"));
        this.maxEnergyStorage = maxEnergyStorage;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new TravelStaffCapabilityProvider(stack);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            ItemStack travelStaff = new ItemStack(this);
            items.add(travelStaff);
            travelStaff = new ItemStack(this);
            this.getOrCreateTag(travelStaff).setInteger("Energy", this.maxEnergyStorage);
            items.add(travelStaff);
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
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
        return getOrCreateTag(item).getInteger("Energy");
    }

    @Override
    public void extractInternal(@Nonnull ItemStack item, int power) {
        //noinspection ConstantConditions
        item.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(power, false);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        int energyStored = getEnergyStored(stack);
        int energyPercent = 100 * energyStored / maxEnergyStorage;
        tooltip.add(String.valueOf(energyPercent > 70 ? TextFormatting.GREEN : energyPercent > 30 ? TextFormatting.YELLOW : TextFormatting.RED) +
                energyStored +
                TextFormatting.GRAY +
                " / " +
                TextFormatting.GREEN +
                maxEnergyStorage +
                TextFormatting.AQUA +
                " FE");
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if (StandaloneConfig.travel.enableBlink) {
                tooltip.add(I18n.format("standalone.travel_staff.blink"));
            }
            tooltip.add(I18n.format("standalone.travel_staff.travel"));
        } else {
            tooltip.add(I18n.format("standalone.hold_shift"));
        }
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        return 1.0D * (maxEnergyStorage - getEnergyStored(stack)) / maxEnergyStorage;
    }

    @Nonnull
    private NBTTagCompound getOrCreateTag(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            stack.setTagCompound(tag = new NBTTagCompound());
        }
        if (!tag.hasKey("Energy")) {
            tag.setInteger("Energy", 0);
        }
        return tag;
    }

    private class TravelStaffCapabilityProvider implements ICapabilityProvider {

        private final ItemStack delegate;

        @SuppressWarnings("FieldMayBeFinal")
        private ItemStack stack;

        ItemEnergyStorage energyStorage;

        public TravelStaffCapabilityProvider(ItemStack stack) {
            this.delegate = stack;
            this.stack = stack;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            if (Loader.isModLoaded("gregtech")) {
                return capability == CapabilityEnergy.ENERGY || capability == Capabilities.GTCE_ENERGY_ITEM;
            }
            return capability == CapabilityEnergy.ENERGY;
        }
        @Nullable
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (energyStorage == null) {
                energyStorage = new ItemEnergyStorage(stack, ItemTravelStaff.this.maxEnergyStorage, Integer.MAX_VALUE, Integer.MAX_VALUE, getEnergyStored(delegate));
            }
            if (capability == Capabilities.GTCE_ENERGY_ITEM) {
                //noinspection unchecked
                return (T) new IElectricItem() {
                    @Override
                    public boolean canProvideChargeExternally() {
                        return energyStorage.canExtract();
                    }

                    @Override
                    public boolean chargeable() {
                        return energyStorage.canReceive();
                    }

                    @Override
                    public void addChargeListener(BiConsumer<ItemStack, Long> biConsumer) {

                    }

                    @Override
                    public long charge(long l, int i, boolean b, boolean b1) {
                        return energyStorage.receiveEnergy((int) l * 4, b1);
                    }

                    @Override
                    public long discharge(long l, int i, boolean b, boolean b1, boolean b2) {
                        return energyStorage.extractEnergy((int) l * 4, b2);
                    }

                    @Override
                    public long getTransferLimit() {
                        return Long.MAX_VALUE;
                    }

                    @Override
                    public long getMaxCharge() {
                        return energyStorage.getMaxEnergyStored() / 4;
                    }

                    @Override
                    public long getCharge() {
                        return energyStorage.getEnergyStored() / 4;
                    }

                    @Override
                    public int getTier() {
                        return 0;
                    }
                };
            }
            //noinspection unchecked
            return (T) energyStorage;
        }
    }

}
