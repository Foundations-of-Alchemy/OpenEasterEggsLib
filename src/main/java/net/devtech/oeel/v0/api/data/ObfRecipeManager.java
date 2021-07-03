package net.devtech.oeel.v0.api.data;

import java.util.ArrayList;
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
import net.devtech.oeel.v0.api.access.HashSubstitution;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class ObfRecipeManager<T extends BaseObfuscatedRecipe> extends MultiJsonDataLoader<T> implements RecipeManager<T> {
	protected static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(BaseObfuscatedRecipe.class, BaseObfuscatedRecipe.SERIALIZER)
			.create();

	protected final Map<HashCode, T> recipes = new HashMap<>();
	protected Collection<HashSubstitution<ItemKey>> itemSubstitutions;
	protected Collection<HashSubstitution<BlockData>> blockSubstitutions;
	protected Collection<HashSubstitution<Entity>> entitySubstitutions;

	public ObfRecipeManager(Ref<T> ref, String dataType, Class<T> type) {
		super(GSON, dataType, type);
		ref.manager = this;
	}

	@Override
	protected void apply(ListMultimap<Identifier, T> prepared) {
		this.recipes.clear();
		Map<Identifier, HashSubstitution<ItemKey>> item = new HashMap<>();
		Map<Identifier, HashSubstitution<BlockData>> block = new HashMap<>();
		Map<Identifier, HashSubstitution<Entity>> entity = new HashMap<>();

		for(Map.Entry<Identifier, List<T>> entry : Multimaps.asMap(prepared).entrySet()) {
			List<T> ts = entry.getValue();
			T last = ts.get(ts.size()-1);
			this.recipes.put(last.inputHash, last);
			var i = last.getItemHashSubstitution();
			if(i != null) {
				item.put(last.itemHashSubstitution, i);
			}

			var b = last.getBlockHashSubstitution();
			if(b != null) {
				block.put(last.blockHashSubstitution, b);
			}

			var e = last.getEntityHashSubstitution();
			if(e != null) {
				entity.put(last.entityHashSubstitution, e);
			}
		}

		this.itemSubstitutions = item.values();
		this.blockSubstitutions = block.values();
		this.entitySubstitutions = entity.values();
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
		public Iterable<HashSubstitution<ItemKey>> allItemSubstitutions() {
			return ((ObfRecipeManager<T>)manager).itemSubstitutions;
		}

		public Iterable<HashSubstitution<BlockData>> allEntitySubstitutions() {
			return ((ObfRecipeManager<T>)manager).blockSubstitutions;
		}

		public Iterable<HashSubstitution<Entity>> allBlockSubstitutions() {
			return ((ObfRecipeManager<T>)manager).entitySubstitutions;
		}
	}
}
