package net.devtech.oeel.v0.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

public class ObfRecipeManager<T extends BaseObfuscatedRecipe> implements RecipeManager<T>, Consumer<ObfResourceManager> {
	protected final Map<HashKey, List<T>> recipeCache = new HashMap<>();
	protected final ByteDeserializer<T> deserializer;
	protected ObfResourceManager current;

	public ObfRecipeManager(ByteDeserializer<T> deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public @Nullable T getForInput(HashKey input, Identifier itemHashFunction, Identifier blockHashFunction, Identifier entityHashFunction) {
		List<T> recipes = this.recipeCache.get(input);
		T retValue = null;
		if(recipes == null) {
			recipes = new ArrayList<>();
			for(T t : this.current.readHeaderObject(input, this.deserializer)) {
				if(t.isValid(input, itemHashFunction, blockHashFunction, entityHashFunction)) {
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
			if(recipe.isValid(input, itemHashFunction, blockHashFunction, entityHashFunction)) {
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
