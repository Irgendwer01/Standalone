package com.cleanroommc.standalone.common.items;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StandaloneItems {

    public static final List<StandaloneItem> ITEMS = new ArrayList<>();
    public static final List<Item> ITEM_BLOCKS = new ArrayList<>();


    private static <T extends StandaloneItem> T register(String id, @Nonnull T item) {
        item.setRegistryName(Standalone.MODID, id);
        ITEMS.add(item);
        return item;
    }

    public static void registerItem(IForgeRegistry<Item> registry) {
        for (Item item : ITEMS)
            registry.register(item);
        for (Item item : ITEM_BLOCKS)
            registry.register(item);
    }

    public static void registerModel() {
        for (StandaloneItem item : ITEMS)
            item.registerModel();
        for (Item item : ITEM_BLOCKS)
            Standalone.proxy.registerItemRenderer(item, 0, "inventory");
    }
}
