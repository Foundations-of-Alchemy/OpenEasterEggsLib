package net.devtech.oeel.v0.api;

import java.util.function.Function;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.impl.hasher.BeaconHasher;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.access.JavaHashFunc;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.data.ObfRecipeManager;
import net.devtech.oeel.v0.api.data.ObfResourceManager;
import net.devtech.oeel.v0.api.event.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.Reg;
import net.devtech.oeel.v0.api.util.hash.Hasher;

import net.minecraft.entity.Entity;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

@SuppressWarnings("unchecked")
public final class OEEL implements ModInitializer {
	public static final Reg<JavaHashFunc<ItemKey>> ITEM_HASHER = new Reg<>();
	public static final Reg<JavaHashFunc<BlockData>> BLOCK_HASHER = new Reg<>();
	public static final Reg<JavaHashFunc<Entity>> ENTITY_HASHER = new Reg<>();
	/**
	 * It's a bit trickier to efficiently grab recipes from here because of the info substitutions. An example implementation is provided at {@link
	 * ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 */
	public static final ObfRecipeManager<BaseObfuscatedRecipe> RECIPES = new ObfRecipeManager<>(BaseObfuscatedRecipe.SERIALIZER);

	@Override
	public void onInitialize() {
		ITEM_HASHER.register(OEELImpl.id("default"), element -> Hasher::putItemKey);
		BLOCK_HASHER.register(OEELImpl.id("default"), element -> (hasher, val) -> hasher.putBlockState(val.getState()));
		ENTITY_HASHER.register(OEELImpl.id("default"), element -> (hasher, val) -> hasher.putRegistry(val.getType(), Registry.ENTITY_TYPE));

		BLOCK_HASHER.register(OEELImpl.id("beacon_hasher"), element -> new BeaconHasher(OEELImpl.GSON.fromJson(element, String[].class)));
		Registry.register(Registry.RECIPE_SERIALIZER, OEELImpl.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);
		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashFunctionManager<>("ihshr", HashFunctionManager.ITEM_COMP));
			manager.registerReloader(new HashFunctionManager<>("bhshr", HashFunctionManager.BLOCK_COMP));
			manager.registerReloader(new HashFunctionManager<>("ehshr", HashFunctionManager.ENTITY_COMP));

			ObfResourceManager resourceManager = new ObfResourceManager();
			RECIPES.accept(resourceManager);

			manager.registerReloader(resourceManager);
		});
	}

}
