package com.cleanroommc.standalone.recipes;

import com.cleanroommc.standalone.api.recipe.RecipeManager;
import com.cleanroommc.standalone.common.blocks.StandaloneBlocks;
import com.cleanroommc.standalone.common.items.StandaloneItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TravelRecipes {

    public static void init() {
        RecipeManager.Crafting.addShapedRecipe("ender_crystal", new ItemStack(StandaloneItems.ENDER_CRYSTAL),
                " E ", "EDE", " E ", 'E', new ItemStack(Items.ENDER_PEARL), 'D', new ItemStack(Items.DIAMOND));

        RecipeManager.Crafting.addShapedRecipe("staff_of_travelling", new ItemStack(StandaloneItems.TRAVEL_STAFF),
                "  C", " I ", "I  ", 'C', new ItemStack(StandaloneItems.ENDER_CRYSTAL), 'I', "ingotIron");

        RecipeManager.Crafting.addShapedRecipe("travel_anchor", new ItemStack(StandaloneBlocks.TRAVEL_ANCHOR),
                "III", "ICI", "III", 'I', "ingotIron", 'C', new ItemStack(StandaloneItems.ENDER_CRYSTAL));
    }
}
