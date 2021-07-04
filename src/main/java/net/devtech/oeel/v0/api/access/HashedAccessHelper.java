package net.devtech.oeel.v0.api.access;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.astrarre.access.v0.api.Access;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.func.IterFunc;
import net.devtech.oeel.impl.resource.HashSubstitutionManager;
import net.devtech.oeel.v0.api.util.OEELHashing;

import net.minecraft.util.Identifier;

public class HashedAccessHelper<F> {
	private final IterFunc<F> func;
	private final Map<Identifier, Entry> entries = new HashMap<>();

	public static <I, F> HashedAccessHelper<F> item(Access<F> func, Function<Function<I, F>, F> and, Function<I, ItemKey> extracter, F empty) {
		return new HashedAccessHelper<>(func.combiner, function -> func.andThen(and.apply(i -> function.apply(extracter.apply(i)))), empty, key -> OEELHashing.hash(key).toString(), HashSubstitutionManager::item);
	}

	public static <F> HashedAccessHelper<F> item(Access<F> func, Function<Function<ItemKey, F>, F> and, F empty) {
		return new HashedAccessHelper<>(func.combiner, function -> func.andThen(and.apply(function)), empty, key -> OEELHashing.hash(key).toString(), HashSubstitutionManager::item);
	}

	public <T> HashedAccessHelper(IterFunc<F> func, Consumer<Function<T, F>> adder, F empty, Function<T, String> hasher, Function<Identifier, HashSubstitution<T>> substitute) {
		this.func = func;
		adder.accept(t -> {
			String origin = hasher.apply(t);
			for(Entry entry : this.entries.values()) {
				var substitution = substitute.apply(entry.id);
				String subst = substitution.substitute(origin, t);
				F function = entry.get(subst);
				if(function != null) {
					return function;
				}
			}
			return empty;
		});
	}

	public HashedAccessHelper<F> forHash(Identifier hashSubstition, String hash, F function) {
		this.entries.computeIfAbsent(hashSubstition, Entry::new).add(hash, function);
		return this;
	}

	public HashedAccessHelper<F> removeForHash(Identifier hashSubstition, String hash, F function) {
		this.entries.computeIfAbsent(hashSubstition, Entry::new).remove(hash, function);
		return this;
	}

	private final class Entry {
		private final Identifier id;
		private final Multimap<String, F> functions = ArrayListMultimap.create();
		private final Map<String, F> compiled = new HashMap<>();

		private Entry(Identifier id) {
			this.id = id;
		}

		public void add(String hashCode, F function) {
			this.functions.put(hashCode, function);
			this.recompile(hashCode);
		}

		public void remove(String hashCode, F function) {
			this.functions.remove(hashCode, function);
			this.recompile(hashCode);
		}

		private void recompile(String hashCode) {
			this.compiled.put(hashCode, HashedAccessHelper.this.func.combine(this.functions.get(hashCode)));
		}

		public F get(String code) {
			return this.compiled.get(code);
		}
	}
}
