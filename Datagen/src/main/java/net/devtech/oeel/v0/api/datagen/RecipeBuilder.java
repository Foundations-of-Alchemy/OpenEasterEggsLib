package net.devtech.oeel.v0.api.datagen;

import java.util.ArrayList;
import java.util.List;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Id;
import net.devtech.oeel.v0.api.access.HashFunction;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;

public class RecipeBuilder {
	static {
		if(!Identifier.class.isAssignableFrom(Id.class)) {
			throw new IllegalStateException("game must be launched!");
		}
	}

	protected final List<ItemStack> stacks = new ArrayList<>();
	protected final HashFunction<ItemKey> itemSubst;

	public RecipeBuilder addStack(ItemStack stack) {
		this.stacks.add(stack);
		return this;
	}

	public RecipeBuilder setItemSubst(Identifier subst) {

	}
}
