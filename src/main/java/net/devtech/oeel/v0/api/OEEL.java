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
import net.devtech.oeel.v0.api.event.ClientResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.event.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.Reg;
import net.devtech.oeel.v0.api.util.hash.Hasher;

import net.minecraft.entity.Entity;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unchecked")
public final class OEEL implements ModInitializer {
	public static final Reg<JavaHashFunc<ItemKey>> ITEM_HASHER = new Reg<>();
	public static final Reg<JavaHashFunc<BlockData>> BLOCK_HASHER = new Reg<>();
	public static final Reg<JavaHashFunc<Entity>> ENTITY_HASHER = new Reg<>();
	/**
	 * It's a bit trickier to efficiently grab recipes from here because of the info substitutions. An example implementation is provided at {@link
	 * ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 */
	public static final ObfRecipeManager<BaseObfuscatedRecipe> RECIPES = new ObfRecipeManager<>(BaseObfuscatedRecipe::new);

	@Override
	public void onInitialize() {
		ITEM_HASHER.register(OEELImpl.id("default"), element -> Hasher::putItemKey);
		BLOCK_HASHER.register(OEELImpl.id("default"), element -> (hasher, val) -> hasher.putBlockState(val.getState()));
		ENTITY_HASHER.register(OEELImpl.id("default"), element -> (hasher, val) -> hasher.putRegistry(val.getType(), Registry.ENTITY_TYPE));
		ITEM_HASHER.register(OEELImpl.id("registry"), element -> (hasher, val) -> hasher.putRegistry(val.getItem(), Registry.ITEM));
		ITEM_HASHER.register(OEELImpl.id("nbt_id"), element -> (hasher, val) -> {
			hasher.putRegistry(val.getItem(), Registry.ITEM);
			hasher.putNbt(val.getTag().getValue("type"));
		});
		BLOCK_HASHER.register(OEELImpl.id("stateless"), element -> (hasher, val) -> hasher.putRegistry(val.getBlock(), Registry.BLOCK));
		BLOCK_HASHER.register(OEELImpl.id("beacon_hasher"), element -> new BeaconHasher(OEELImpl.GSON.fromJson(element, String[].class)));

		Registry.register(Registry.RECIPE_SERIALIZER, OEELImpl.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);
		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashFunctionManager<>("ihshr", HashFunctionManager.SERVER.itemComp));
			manager.registerReloader(new HashFunctionManager<>("bhshr", HashFunctionManager.SERVER.blockComp));
			manager.registerReloader(new HashFunctionManager<>("ehshr", HashFunctionManager.SERVER.entityComp));

			ObfResourceManager resourceManager = new ObfResourceManager();
			RECIPES.accept(resourceManager);

			manager.registerReloader(resourceManager);
		});

		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientResourceManagerLoadEvent.POST_ALL.andThen((client, manager) -> {
				ObfResourceManager resourceManager = new ObfResourceManager();
				manager.registerReloader(resourceManager);
				manager.registerReloader(new HashFunctionManager<>("ihshr", HashFunctionManager.CLIENT.itemComp));
				manager.registerReloader(new HashFunctionManager<>("bhshr", HashFunctionManager.CLIENT.blockComp));
				manager.registerReloader(new HashFunctionManager<>("ehshr", HashFunctionManager.CLIENT.entityComp));
				ObfResourceManager.client = resourceManager;
			});
		}
	}
}
