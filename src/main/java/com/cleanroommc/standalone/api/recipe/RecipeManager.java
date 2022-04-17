package com.cleanroommc.standalone.api.recipe;

import com.cleanroommc.standalone.Standalone;
import com.cleanroommc.standalone.api.StandaloneValues;
import com.cleanroommc.standalone.utils.StandaloneLog;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RecipeManager {

    public static class Furnace {

        /**
         * Add a simple Furnace Recipe with no experience
         *
         * @param input  the input of the recipe
         * @param output the output of the recipe
         */
        public static void addSmeltingRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output) {
            addSmeltingRecipe(input, output, 0.0F);
        }

        /**
         * Add a simple Furnace recipe with experience
         *
         * @param input      the input of the recipe
         * @param output     the output of the recipe
         * @param experience the experience gained from the recipe
         */
        public static void addSmeltingRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output, float experience) {
            boolean skip = false;
            if (input.isEmpty()) {
                StandaloneLog.logger.error("Input cannot be an empty ItemStack", new IllegalArgumentException());
                skip = true;
            }
            if (output.isEmpty()) {
                StandaloneLog.logger.error("Output cannot be an empty ItemStack", new IllegalArgumentException());
                skip = true;
            }
            if (skip) return;
            FurnaceRecipes recipes = FurnaceRecipes.instance();

            if (recipes.getSmeltingResult(input).isEmpty()) {
                //register only if there is no recipe with duplicate input
                recipes.addSmeltingRecipe(input, output, experience);
            }
        }

        /**
         * Removes a simple Furnace Recipe
         * @param input the input of the recipe
         * @return {@code true} if the recipe was removed, otherwise {@code false}
         */
        public static boolean removeFurnaceSmeltingRecipe(@Nonnull ItemStack input) {
            if (input.isEmpty()) {
                StandaloneLog.logger.error("Cannot remove furnace recipe with empty input.");
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                return false;
            }

            boolean wasRemoved = FurnaceRecipes.instance().getSmeltingList().keySet()
                    .removeIf(currentStack -> currentStack.getItem() == input.getItem() &&
                            (currentStack.getMetadata() == StandaloneValues.W || currentStack.getMetadata() == input.getMetadata()));

            if (StandaloneValues.DEBUG) {
                if (wasRemoved) StandaloneLog.logger.info("Removed Smelting Recipe for Input: {}", input.getDisplayName());
                else StandaloneLog.logger.error("Failed to Remove Smelting Recipe for Input: {}", input.getDisplayName());
            }

            return wasRemoved;
        }
    }

    public static class Crafting {

        public static void addMirroredShapedRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
            boolean skip = false;
            if (result.isEmpty()) {
                StandaloneLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            }
            if (skip | validateRecipe(regName, recipe)) return;

            IRecipe shapedOreRecipe = new ShapedOreRecipe(new ResourceLocation(Standalone.MODID, "general"), result.copy(), finalizeShapedRecipeInput(recipe))
                    .setMirrored(true)
                    .setRegistryName(regName);
            ForgeRegistries.RECIPES.register(shapedOreRecipe);

        }

        public static void addShapedRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
            boolean skip = false;
            if (result.isEmpty()) {
                StandaloneLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            }

            if (skip | validateRecipe(regName, recipe)) return;

            IRecipe shapedOreRecipe = new ShapedOreRecipe(null, result.copy(), finalizeShapedRecipeInput(recipe))
                    .setMirrored(false) //make all recipes not mirrored by default
                    .setRegistryName(regName);
            ForgeRegistries.RECIPES.register(shapedOreRecipe);
        }

        private static boolean validateRecipe(@Nonnull String regName, Object... recipe) {
            boolean skip = false;
            if (recipe == null) {
                StandaloneLog.logger.error("Recipe cannot be null", new IllegalArgumentException());
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            } else if (recipe.length == 0) {
                StandaloneLog.logger.error("Recipe cannot be empty", new IllegalArgumentException());
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            } else if (Arrays.asList(recipe).contains(null) || Arrays.asList(recipe).contains(ItemStack.EMPTY)) {
                StandaloneLog.logger.error("Recipe cannot contain null elements or Empty ItemStacks. Recipe: {}",
                        Arrays.stream(recipe)
                                .map(o -> o == null ? "NULL" : o)
                                .map(o -> o == ItemStack.EMPTY ? "EMPTY STACK" : o)
                                .map(Object::toString)
                                .map(s -> "\"" + s + "\"")
                                .collect(Collectors.joining(", ")));
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            } else if (ForgeRegistries.RECIPES.containsKey(new ResourceLocation(Standalone.MODID, regName))) {
                StandaloneLog.logger.error("Tried to register recipe, {}, with duplicate key. Recipe: {}", regName,
                        Arrays.stream(recipe)
                                .map(Object::toString)
                                .map(s -> "\"" + s + "\"")
                                .collect(Collectors.joining(", ")));
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            }
            return skip;
        }

        @Nonnull
        public static Object[] finalizeShapedRecipeInput(@Nonnull Object... recipe) {
            for (byte i = 0; i < recipe.length; i++) {
                recipe[i] = finalizeIngredient(recipe[i]);
            }
            int idx = 0;
            ArrayList<Object> recipeList = new ArrayList<>(Arrays.asList(recipe));

            while (recipe[idx] instanceof String) {
                StringBuilder s = new StringBuilder((String) recipe[idx++]);
                while (s.length() < 3) s.append(" ");
                if (s.length() > 3) throw new IllegalArgumentException();
            }
            return recipeList.toArray();
        }

        public static Object finalizeIngredient(Object ingredient) {
            if (ingredient instanceof Enum) {
                ingredient = ((Enum<?>) ingredient).name();
            } else if (!(ingredient instanceof ItemStack
                    || ingredient instanceof Item
                    || ingredient instanceof Block
                    || ingredient instanceof String
                    || ingredient instanceof Character
                    || ingredient instanceof Boolean
                    || ingredient instanceof Ingredient)) {
                throw new IllegalArgumentException(ingredient.getClass().getSimpleName() + " type is not suitable for crafting input.");
            }
            return ingredient;
        }

        public static void addShapelessRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
            boolean skip = false;
            if (result.isEmpty()) {
                StandaloneLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
                StandaloneLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            }

            if (skip | validateRecipe(regName, recipe)) return;

            for (byte i = 0; i < recipe.length; i++) {
                if (recipe[i] instanceof Enum) {
                    recipe[i] = ((Enum<?>) recipe[i]).name();
                } else if (!(recipe[i] instanceof ItemStack
                        || recipe[i] instanceof Item
                        || recipe[i] instanceof Block
                        || recipe[i] instanceof String)) {
                    throw new IllegalArgumentException(recipe.getClass().getSimpleName() + " type is not suitable for crafting input.");
                }
            }

            IRecipe shapelessRecipe = new ShapelessOreRecipe(null, result.copy(), recipe).setRegistryName(regName);

            try {
                //workaround for MC bug that makes all shaped recipe inputs that have enchanted items
                //or renamed ones on input fail, even if all ingredients match it
                Field field = ShapelessOreRecipe.class.getDeclaredField("isSimple");
                field.setAccessible(true);
                field.setBoolean(shapelessRecipe, false);
            } catch (ReflectiveOperationException exception) {
                StandaloneLog.logger.error("Failed to mark shapeless recipe as complex", exception);
            }

            ForgeRegistries.RECIPES.register(shapelessRecipe);
        }

        public static int removeRecipes(ItemStack output) {
            int recipesRemoved = removeRecipes(recipe -> ItemStack.areItemStacksEqual(recipe.getRecipeOutput(), output));

            if (StandaloneValues.DEBUG) {
                if (recipesRemoved != 0) StandaloneLog.logger.info("Removed {} Recipe(s) with Output: {}", recipesRemoved, output.getDisplayName());
                else StandaloneLog.logger.error("Failed to Remove Recipe with Output: {}", output.getDisplayName());
            }
            return recipesRemoved;
        }

        public static int removeRecipes(Predicate<IRecipe> predicate) {
            int recipesRemoved = 0;
            IForgeRegistry<IRecipe> registry = ForgeRegistries.RECIPES;
            List<IRecipe> toRemove = new ArrayList<>();

            for (IRecipe recipe : registry) {
                if (predicate.test(recipe)) {
                    toRemove.add(recipe);
                    recipesRemoved++;
                }
            }

            //noinspection ConstantConditions
            toRemove.forEach(recipe -> registry.register(new DummyRecipe().setRegistryName(recipe.getRegistryName())));
            return recipesRemoved;
        }

        /**
         * Removes a Crafting Table Recipe with the given name.
         *
         * @param location The ResourceLocation of the Recipe. Can also accept a String.
         */
        public static void removeRecipeByName(ResourceLocation location) {
            if (StandaloneValues.DEBUG) {
                String recipeName = location.toString();
                if (ForgeRegistries.RECIPES.containsKey(location)) StandaloneLog.logger.info("Removed Recipe with Name: {}", recipeName);
                else StandaloneLog.logger.error("Failed to Remove Recipe with Name: {}", recipeName);
            }
            ForgeRegistries.RECIPES.register(new DummyRecipe().setRegistryName(location));
        }

        public static void removeRecipeByName(String recipeName) {
            removeRecipeByName(new ResourceLocation(recipeName));
        }
    }
}
