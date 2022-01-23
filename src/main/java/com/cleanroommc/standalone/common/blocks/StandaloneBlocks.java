package com.cleanroommc.standalone.common.blocks;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneBlock;
import com.cleanroommc.standalone.common.blocks.travelanchor.BlockTravelAnchor;
import com.cleanroommc.standalone.common.items.StandaloneItems;
import com.cleanroommc.standalone.common.tileentity.travelanchor.TileEntityTravelAnchor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StandaloneBlocks {

    private static final List<StandaloneBlock> BLOCKS = new ArrayList<>();
    private static final Map<Class<? extends TileEntity>, String> TILE_ENTITIES = new Object2ObjectOpenHashMap<>();

    public static final BlockTravelAnchor TRAVEL_ANCHOR = register("travel_anchor", new BlockTravelAnchor(), TileEntityTravelAnchor.class);


    private static <T extends StandaloneBlock> T register(String id, @Nonnull T block) {
        block.setRegistryName(Standalone.MODID, id);
        BLOCKS.add(block);
        StandaloneItems.ITEM_BLOCKS.add(new ItemBlock(block).setRegistryName(Objects.requireNonNull(block.getRegistryName())));
        return block;
    }

    @Nonnull
    private static <T extends StandaloneBlock, TE extends TileEntity> T register(String id, T block, Class<TE> tileEntity) {
        T registeredBlock = register(id, block);
        TILE_ENTITIES.put(tileEntity, id);
        return registeredBlock;
    }

    private static void register(String id, Class<? extends TileEntity> tileEntity) {
        TILE_ENTITIES.put(tileEntity, id);
    }

    public static void registerBlock(IForgeRegistry<Block> registry) {
        for (Block block : BLOCKS) {
            registry.register(block);
        }

        for (Map.Entry<Class<? extends TileEntity>, String> entry : TILE_ENTITIES.entrySet()) {
            GameRegistry.registerTileEntity(entry.getKey(), new ResourceLocation(Standalone.MODID, entry.getValue()));
        }
    }

    public static void registerModel() {
        for (StandaloneBlock block : BLOCKS)
            block.registerModel();
    }

}
