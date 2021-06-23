package net.devtech.oeel.v0.api.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.recipes.ObfuscatedItemRecipe;

import net.minecraft.util.Identifier;

public class ObfRecipeManager<T extends BaseObfuscatedRecipe> extends MultiJsonDataLoader<T> implements RecipeManager<T> {
	protected static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(BaseObfuscatedRecipe.class, BaseObfuscatedRecipe.SERIALIZER)
			.registerTypeAdapter(ObfuscatedItemRecipe.class, ObfuscatedItemRecipe.SERIALIZER)
			.create();

	public final Map<HashCode, T> recipes = new HashMap<>();

	public ObfRecipeManager(Ref<T> ref, String dataType, Class<T> type) {
		super(GSON, dataType, type);
		ref.manager = this;
	}

	@Override
	protected void apply(ListMultimap<Identifier, T> prepared) {
		this.recipes.clear();
		for(Map.Entry<Identifier, List<T>> entry : Multimaps.asMap(prepared).entrySet()) {
			List<T> ts = entry.getValue();
			T last = ts.get(ts.size()-1);
			this.recipes.put(last.inputHash, last);
		}
	}

	@Override
	public T getForInput(HashCode input) {
		return this.recipes.get(input);
	}

	@Override
	public Collection<T> getAll() {
		return this.recipes.values();
	}
}
