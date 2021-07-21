package net.devtech.oeel.v0.api;

import java.util.function.Function;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELInternal;
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

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

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

	public static void init() {
		/*ItemLangOverrideEvent.EVENT.andThen(stack -> {
			BiHasher hasher = BiHasher.createDefault(false);
			hasher.putItem(stack);
			hasher.putString("standard", StandardCharsets.US_ASCII);
			return OEELInternal.LANG_STARTER + hasher.hashA() + "." + hasher.hashB();
		});*/

		BLOCK_HASHER.register(new Identifier("oeel:beacon_hasher"), element -> new BeaconHasher(OEELInternal.GSON.fromJson(element, String[].class)));
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);
		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashFunctionManager<>("ihshr",HashFunctionManager.BLOCK_COMP));
			manager.registerReloader(new HashFunctionManager<>("bhshr",HashFunctionManager.BLOCK_COMP));
			manager.registerReloader(new HashFunctionManager<>("ehshr",HashFunctionManager.ENTITY_COMP));
			ObfResourceManager resourceManager = new ObfResourceManager();
			RECIPES.accept(resourceManager);
			manager.registerReloader(resourceManager);
		});
	}

	@Override
	public void onInitialize() {
		init();
	}

}
