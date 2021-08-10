package net.devtech.oeel.v0.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.devtech.oeel.v0.api.access.OEELDeserializer;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

public class ObfRecipeManager<T extends BaseObfuscatedRecipe> implements RecipeManager<T>, Consumer<ObfResourceManager> {
	protected final Map<HashKey, List<T>> recipeCache = new HashMap<>();
	protected final OEELDeserializer<T> deserializer;
	protected ObfResourceManager current;

	public ObfRecipeManager(OEELDeserializer<T> deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public @Nullable T getForInput(HashKey input, byte[] key, Identifier itemHashFunction, Identifier blockHashFunction, Identifier entityHashFunction) {
		List<T> recipes = this.recipeCache.get(input);
		T retValue = null;
		if(recipes == null) {
			recipes = new ArrayList<>();
			for(T t : this.current.decryptOnce(input, key, this.deserializer)) {
				if(t.isValid(itemHashFunction, blockHashFunction, entityHashFunction)) {
					retValue = t;
				}
				recipes.add(t);
			}
			this.recipeCache.put(input, recipes);
		}

		if(retValue != null) {
			return retValue;
		}

		for(T recipe : recipes) {
			if(recipe.isValid(itemHashFunction, blockHashFunction, entityHashFunction)) {
				return recipe;
			}
		}
		return null;
	}

	@Override
	public void accept(ObfResourceManager manager) {
		this.recipeCache.clear();
		this.current = manager;
	}
}
