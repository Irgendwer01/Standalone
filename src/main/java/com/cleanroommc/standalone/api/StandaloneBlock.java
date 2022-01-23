package com.cleanroommc.standalone.api;

import com.cleanroommc.standalone.Standalone;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class StandaloneBlock extends Block {

    private final BlockSettings settings;

    public StandaloneBlock(@Nonnull BlockSettings settings) {
        super(settings.material, settings.material.getMaterialMapColor());
        this.settings = settings;

        setResistance(settings.resistance);
        setHardness(settings.hardness);
        setSoundType(settings.soundType);
        setTranslationKey(settings.translationKey);
        setCreativeTab(settings.tab);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return settings.mapColor != null ? settings.mapColor.apply(state, worldIn, pos) : settings.material.getMaterialMapColor();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return isOpaqueCube(state) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return settings != null && settings.opaque;
    }

    @Override
    public boolean isCollidable() {
        return settings.collidable;
    }

    @Override
    public float getSlipperiness(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        return settings.slipperiness.apply(state, world, pos);
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return settings.lightValue.apply(state, world, pos);
    }

    public void registerModel() {
        Standalone.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }

    @FunctionalInterface
    public interface ContextFunction<R> {
        R apply(IBlockState t1, IBlockAccess t2, BlockPos t3);
    }

    public static class BlockSettings {

        final Material material;
        final ContextFunction<MapColor> mapColor;
        String translationKey;
        CreativeTabs tab;
        boolean collidable = true;
        boolean opaque = true;
        float resistance;
        float hardness;
        SoundType soundType = SoundType.STONE;
        ContextFunction<Integer> lightValue = (state, access, pos) -> 0;
        ContextFunction<Float> slipperiness = (state, access, pos) -> 0.6F;

        public BlockSettings(Material material) {
            this(material, material.getMaterialMapColor());
        }

        public BlockSettings(Material material, MapColor mapColor) {
            this(material, (state, access, pos) -> mapColor);
        }

        public BlockSettings(Material material, ContextFunction<MapColor> mapColor) {
            this.material = material;
            this.mapColor = mapColor;
        }

        @Nonnull
        public static BlockSettings copy(@Nonnull StandaloneBlock block) {
            BlockSettings blockSettings = new BlockSettings(block.settings.material, block.blockMapColor);
            blockSettings.collidable = block.settings.collidable;
            blockSettings.opaque = block.settings.opaque;
            blockSettings.soundType = block.settings.soundType;
            blockSettings.lightValue = block.settings.lightValue;
            blockSettings.slipperiness = block.settings.slipperiness;
            return blockSettings;
        }

        public BlockSettings noCollision() {
            collidable = false;
            opaque = false;
            return this;
        }

        public BlockSettings nonOpaque() {
            opaque = false;
            return this;
        }

        public BlockSettings translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public BlockSettings creativeTab(CreativeTabs tab) {
            this.tab = tab;
            return this;
        }

        public BlockSettings strength(float strength) {
            resistance = strength;
            hardness = strength;
            return this;
        }

        public BlockSettings resistance(float resistance) {
            this.resistance = resistance;
            return this;
        }

        public BlockSettings hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public BlockSettings soundType(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

        public BlockSettings lightValue(ContextFunction<Integer> lightValue) {
            this.lightValue = lightValue;
            return this;
        }

        public BlockSettings lightValue(Function<IBlockState, Integer> lightValue) {
            this.lightValue = (state, access, pos) -> lightValue.apply(state);
            return this;
        }

        public BlockSettings lightValue(int lightValue) {
            this.lightValue = (state, access, pos) -> lightValue;
            return this;
        }

        public BlockSettings slipperiness(ContextFunction<Float> slipperiness) {
            this.slipperiness = slipperiness;
            return this;
        }

        public BlockSettings slipperiness(Function<IBlockState, Float> slipperiness) {
            this.slipperiness = (state, access, pos) -> slipperiness.apply(state);
            return this;
        }

        public BlockSettings slipperiness(float slipperiness) {
            this.slipperiness = (state, access, pos) -> slipperiness;
            return this;
        }

    }
}
