package com.cleanroommc.standalone.api;

import com.cleanroommc.standalone.Standalone;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IRarity;

import javax.annotation.Nonnull;

public class StandaloneItem extends Item {

    EnumRarity rarity;

    public StandaloneItem(@Nonnull ItemSettings settings) {
        setMaxStackSize(settings.maxCount);
        setMaxDamage(settings.maxDamage);
        setCreativeTab(settings.tab);
        rarity = settings.rarity;
        setTranslationKey(settings.translationKey);
    }

    @Nonnull
    @Override
    public IRarity getForgeRarity(@Nonnull ItemStack stack) {
        return rarity;
    }

    public void registerModel() {
        Standalone.proxy.registerItemRenderer(this, 0, "inventory");
    }

    public static class ItemSettings {

        int maxCount = 64;
        int maxDamage;
        CreativeTabs tab;
        EnumRarity rarity;
        String translationKey;

        public ItemSettings() {
            rarity = EnumRarity.COMMON;
        }

        public ItemSettings maxCount(int count) {
            maxCount = count;
            return this;
        }

        public ItemSettings maxDamage(int damage) {
            maxCount = damage;
            return this;
        }

        public ItemSettings creativeTab(CreativeTabs tab) {
            this.tab = tab;
            return this;
        }

        public ItemSettings translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

    }

}
