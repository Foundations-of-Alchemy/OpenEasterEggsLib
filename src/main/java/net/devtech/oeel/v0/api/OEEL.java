package net.devtech.oeel.v0.api;

import java.util.function.Function;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.resource.HashSubstitutionManager;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.data.ObfRecipeManager;
import net.devtech.oeel.v0.api.data.RecipeManager;
import net.devtech.oeel.v0.api.data.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;

import net.minecraft.entity.Entity;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public final class OEEL implements ModInitializer {

	/**
	 * If you use this system
	 *
	 * It's a bit trickier to efficiently grab recipes from here because of the hash substitutions. An example implementation is provided at {@link
	 * ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 *
	 * You may want to make a separate manager.
	 */
	public static final ObfRecipeManager.Ref<BaseObfuscatedRecipe> RECIPES = new ObfRecipeManager.Ref<>();

	@Override
	public void onInitialize() {
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);

		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashSubstitutionManager<>("item_subst",
			                                                       HashSubstitutionManager.ITEM_HASH_FUNCTIONS,
			                                                       Registry.ITEM_KEY,
			                                                       ItemKey::getItem));
			manager.registerReloader(new HashSubstitutionManager<>("block_subst",
			                                                       HashSubstitutionManager.BLOCK_HASH_FUNCTIONS,
			                                                       Registry.BLOCK_KEY,
			                                                       b -> b.getState().getBlock()));
			manager.registerReloader(new HashSubstitutionManager<>("entity_subst",
			                                                       HashSubstitutionManager.ENTITY_HASH_FUNCTIONS,
			                                                       Registry.ENTITY_TYPE_KEY,
			                                                       Entity::getType));

			manager.registerReloader(new ObfRecipeManager<>(RECIPES, "obf_base", BaseObfuscatedRecipe.class));
		});
	}

}
