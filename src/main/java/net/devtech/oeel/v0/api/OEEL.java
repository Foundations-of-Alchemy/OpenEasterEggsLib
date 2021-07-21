package net.devtech.oeel.v0.api;

import java.util.function.Function;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.hasher.BeaconHasher;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.access.DynamicHashFunction;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.data.ObfRecipeManager;
import net.devtech.oeel.v0.api.data.ObfResourceManager;
import net.devtech.oeel.v0.api.event.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unchecked")
public final class OEEL implements ModInitializer {
	public static final Registry<DynamicHashFunction<ItemKey>> ITEM_HASHER = FabricRegistryBuilder.createSimple((Class) DynamicHashFunction.class, new Identifier("oeel:item_hashers")).buildAndRegister();
	public static final Registry<DynamicHashFunction<BlockData>> BLOCK_HASHER = FabricRegistryBuilder.createSimple((Class) DynamicHashFunction.class, new Identifier("oeel:block_hashers")).buildAndRegister();
	public static final Registry<DynamicHashFunction<Entity>> ENTITY_HASHER = FabricRegistryBuilder.createSimple((Class) DynamicHashFunction.class, new Identifier("oeel:entity_hashers")).buildAndRegister();
	/**
	 *
	 * It's a bit trickier to efficiently grab recipes from here because of the info substitutions.
	 * An example implementation is provided at {@link ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 *
	 */
	public static final ObfRecipeManager<BaseObfuscatedRecipe> RECIPES = new ObfRecipeManager<>(BaseObfuscatedRecipe.SERIALIZER);

	public static final Identifier DEFAULT_HASHES = new Identifier("oeel:standard");

	public static void init() {
		/*ItemLangOverrideEvent.EVENT.andThen(stack -> {
			BiHasher hasher = BiHasher.createDefault(false);
			hasher.putItem(stack);
			hasher.putString("standard", StandardCharsets.US_ASCII);
			return OEELInternal.LANG_STARTER + hasher.hashA() + "." + hasher.hashB();
		});*/

		Registry.register(BLOCK_HASHER, new Identifier("oeel:beacon_hasher"), element -> new BeaconHasher(OEELInternal.GSON.fromJson(element, String[].class)));
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);
		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashFunctionManager<>("item_func",
			                                                   HashFunctionManager.ITEM_HASH_FUNCTIONS,
			                                                   Registry.ITEM_KEY,
			                                                   ItemKey::getItem, ITEM_HASHER));
			manager.registerReloader(new HashFunctionManager<>("block_func",
			                                                   HashFunctionManager.BLOCK_HASH_FUNCTIONS,
			                                                   Registry.BLOCK_KEY,
			                                                   b -> b.getState().getBlock(), BLOCK_HASHER));
			manager.registerReloader(new HashFunctionManager<>("entity_func",
			                                                   HashFunctionManager.ENTITY_HASH_FUNCTIONS,
			                                                   Registry.ENTITY_TYPE_KEY,
			                                                   Entity::getType, ENTITY_HASHER));
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
