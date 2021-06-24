package net.devtech.oeel.v0.api;

import java.util.function.Function;

import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.resource.ItemHashSubstitutionManager;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.data.ObfRecipeManager;
import net.devtech.oeel.v0.api.data.RecipeManager;
import net.devtech.oeel.v0.api.data.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.recipes.ObfuscatedItemRecipe;

import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public final class OEEL implements ModInitializer {
	public static final RecipeManager.Ref<BaseObfuscatedRecipe> BASE_RECIPES = new RecipeManager.Ref<>();

	/**
	 * It's a bit trickier to efficiently grab recipes from here because of the hash substitutions.
	 * An example implementation is provided at {@link ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 *
	 * You may want to make a separate manager.
	 */
	public static final RecipeManager.Ref<ObfuscatedItemRecipe> ITEM_RECIPES = new RecipeManager.Ref<>();

	@Override
	public void onInitialize() {
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);

		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new ItemHashSubstitutionManager());
			manager.registerReloader(new ObfRecipeManager<>(BASE_RECIPES, "obf_base", BaseObfuscatedRecipe.class));
			manager.registerReloader(new ObfRecipeManager<>(ITEM_RECIPES, "obf_item", ObfuscatedItemRecipe.class));
		});
	}
}
