package net.devtech.oeel.v0.api;

import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.ServerResourceManagerLoadEvent;
import net.devtech.oeel.impl.resource.ItemHashSubstitutionManager;
import net.devtech.oeel.impl.shaped.ObfuscatedShapedCraftingRecipe;
import net.devtech.oeel.impl.shaped.ObfuscatedShapelessCraftingRecipe;

import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public final class OEEL implements ModInitializer {
	@Override
	public void onInitialize() {
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_shaped"), ObfuscatedShapedCraftingRecipe.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, OEELInternal.id("obf_shapeless"), ObfuscatedShapelessCraftingRecipe.SERIALIZER);

		ServerResourceManagerLoadEvent.POST_TAG.andThen((serverResourceManager, manager) -> manager.registerReloader(new ItemHashSubstitutionManager()));
	}
}
