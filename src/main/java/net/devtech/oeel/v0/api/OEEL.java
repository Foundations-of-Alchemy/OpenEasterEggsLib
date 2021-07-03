package net.devtech.oeel.v0.api;

import java.util.function.Function;

import com.google.gson.Gson;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.hasher.BeaconHasher;
import net.devtech.oeel.impl.resource.HashSubstitutionManager;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.access.DynamicHashSubstitution;
import net.devtech.oeel.v0.api.data.ObfRecipeManager;
import net.devtech.oeel.v0.api.data.ServerResourceManagerLoadEvent;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

@SuppressWarnings("unchecked")
public final class OEEL implements ModInitializer {
	private static final Gson GSON = new Gson();
	public static final Registry<DynamicHashSubstitution<ItemKey>> ITEM_HASHER = FabricRegistryBuilder.createSimple((Class)DynamicHashSubstitution.class, new Identifier("oeel:item_hashers")).buildAndRegister();
	public static final Registry<DynamicHashSubstitution<BlockData>> BLOCK_HASHER = FabricRegistryBuilder.createSimple((Class)DynamicHashSubstitution.class, new Identifier("oeel:item_hashers")).buildAndRegister();
	public static final Registry<DynamicHashSubstitution<Entity>> ENTITY_HASHER = FabricRegistryBuilder.createSimple((Class)DynamicHashSubstitution.class, new Identifier("oeel:item_hashers")).buildAndRegister();

	/**
	 *
	 * It's a bit trickier to efficiently grab recipes from here because of the hash substitutions.
	 * An example implementation is provided at {@link ObfuscatedCraftingRecipeBridge#craft(boolean, Function)}.
	 *
	 */
	public static final ObfRecipeManager.Ref<BaseObfuscatedRecipe> RECIPES = new ObfRecipeManager.Ref<>();

	@Override
	public void onInitialize() {
		Registry.register(BLOCK_HASHER, new Identifier("oeel:beacon_hasher"), element -> new BeaconHasher(GSON.fromJson(element, String[].class)));

		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_crafting"), ObfuscatedCraftingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedStonecuttingRecipeBridge.ID, ObfuscatedStonecuttingRecipeBridge.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, ObfuscatedSmithingRecipeBridge.ID, ObfuscatedSmithingRecipeBridge.SERIALIZER);

		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> {
			manager.registerReloader(new HashSubstitutionManager<>("item_subst",
			                                                       HashSubstitutionManager.ITEM_HASH_FUNCTIONS,
			                                                       Registry.ITEM_KEY,
			                                                       ItemKey::getItem, ITEM_HASHER));
			manager.registerReloader(new HashSubstitutionManager<>("block_subst",
			                                                       HashSubstitutionManager.BLOCK_HASH_FUNCTIONS,
			                                                       Registry.BLOCK_KEY,
			                                                       b -> b.getState().getBlock(), BLOCK_HASHER));
			manager.registerReloader(new HashSubstitutionManager<>("entity_subst",
			                                                       HashSubstitutionManager.ENTITY_HASH_FUNCTIONS,
			                                                       Registry.ENTITY_TYPE_KEY,
			                                                       Entity::getType, ENTITY_HASHER));

			manager.registerReloader(new ObfRecipeManager<>(RECIPES, "obf_base", BaseObfuscatedRecipe.class));
		});
	}

}
