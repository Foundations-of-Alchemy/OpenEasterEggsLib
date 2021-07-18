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
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class ObfRecipeManager<T extends BaseObfuscatedRecipe> extends MultiJsonDataLoader<T> implements RecipeManager<T> {
	protected static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(BaseObfuscatedRecipe.class, BaseObfuscatedRecipe.SERIALIZER)
			.create();

	protected final Map<HashCode, T> recipes = new HashMap<>();
	protected Collection<HashFunction<ItemKey>> itemFunction;
	protected Collection<HashFunction<BlockData>> blockFunction;
	protected Collection<HashFunction<Entity>> entityFunction;

	public ObfRecipeManager(Ref<T> ref, String dataType, Class<T> type) {
		super(GSON, dataType, type);
		ref.manager = this;
	}

	@Override
	protected void apply(ListMultimap<Identifier, T> prepared) {
		this.recipes.clear();
		Map<Identifier, HashFunction<ItemKey>> item = new HashMap<>();
		Map<Identifier, HashFunction<BlockData>> block = new HashMap<>();
		Map<Identifier, HashFunction<Entity>> entity = new HashMap<>();

		for(Map.Entry<Identifier, List<T>> entry : Multimaps.asMap(prepared).entrySet()) {
			List<T> ts = entry.getValue();
			T last = ts.get(ts.size()-1);
			this.recipes.put(last.inputHash, last);
			var i = last.getItemHashFunction();
			if(i != null) {
				item.put(last.itemHashFunction, i);
			}

			var b = last.getBlockHashFunction();
			if(b != null) {
				block.put(last.blockHashFunction, b);
			}

			var e = last.getEntityHashFunction();
			if(e != null) {
				entity.put(last.entityHashFunction, e);
			}
		}

		this.itemFunction = item.values();
		this.blockFunction = block.values();
		this.entityFunction = entity.values();
	}

	@Override
	public T getForInput(HashCode input) {
		return this.recipes.get(input);
	}

	@Override
	public Collection<T> getAll() {
		return this.recipes.values();
	}

	public static class Ref<T extends BaseObfuscatedRecipe> extends RecipeManager.Ref<T> {
		public Iterable<HashFunction<ItemKey>> allItemFunctions() {
			return ((ObfRecipeManager<T>)manager).itemFunction;
		}

		public Iterable<HashFunction<BlockData>> allEntityFunctions() {
			return ((ObfRecipeManager<T>)manager).blockFunction;
		}

		public Iterable<HashFunction<Entity>> allBlockFunctions() {
			return ((ObfRecipeManager<T>)manager).entityFunction;
		}
	}
}
