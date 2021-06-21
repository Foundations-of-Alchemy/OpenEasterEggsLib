package net.devtech.oeel.v0.api;

import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.access.ItemHasher;

import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;

public final class OEEL {
	public static final Registry<ItemHasher> HASHERS = FabricRegistryBuilder.createDefaulted(ItemHasher.class,
	                                                                                         OEELInternal.id("custom_hashers"),
	                                                                                         OEELInternal.id("default")).buildAndRegister();
	static {
		Registry.register(HASHERS, OEELInternal.id("default"), (stack, hasher) -> {
			OEELInternal.hash(hasher, Registry.ITEM, stack.getItem());
			OEELInternal.hash(hasher, stack.getTag().asMinecraft());
			return true;
		});
	}

}
