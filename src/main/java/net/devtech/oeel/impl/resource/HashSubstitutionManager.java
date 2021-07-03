package net.devtech.oeel.impl.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.HashSubstitution;
import net.devtech.oeel.v0.api.data.MultiJsonDataLoader;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.entity.Entity;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

@SuppressWarnings("unchecked")
public class HashSubstitutionManager<T, E> extends MultiJsonDataLoader<JsonElement> {
	public static final Map<Identifier, HashSubstitution<ItemKey>> ITEM_HASH_FUNCTIONS = new HashMap<>();
	public static final Map<Identifier, HashSubstitution<Entity>> ENTITY_HASH_FUNCTIONS = new HashMap<>();
	public static final Map<Identifier, HashSubstitution<BlockData>> BLOCK_HASH_FUNCTIONS = new HashMap<>();

	private final Map<Identifier, HashSubstitution<T>> map;
	private final Function<Identifier, HashSubstitution<T>> func;
	private final RegistryKey<Registry<E>> registry;
	private final Function<T, E> getter;

	public HashSubstitutionManager(String dataType,
			Map<Identifier, HashSubstitution<T>> map,
			RegistryKey<Registry<E>> registry,
			Function<T, E> getter) {
		super(dataType, JsonElement.class);
		this.map = map;
		this.registry = registry;
		this.getter = getter;
		this.func = id1 -> new HasherRef<>(this.map, id1);
	}

	public static HashSubstitution<ItemKey> item(Identifier substConfigId) {
		return ITEM_HASH_FUNCTIONS.computeIfAbsent(substConfigId, id -> new HasherRef<>(ITEM_HASH_FUNCTIONS, id));
	}

	public static HashSubstitution<Entity> entity(Identifier substConfigId) {
		return ENTITY_HASH_FUNCTIONS.computeIfAbsent(substConfigId, id -> new HasherRef<>(ENTITY_HASH_FUNCTIONS, id));
	}

	public static HashSubstitution<BlockData> block(Identifier substConfigId) {
		return BLOCK_HASH_FUNCTIONS.computeIfAbsent(substConfigId, id -> new HasherRef<>(BLOCK_HASH_FUNCTIONS, id));
	}

	@Override
	protected void apply(ListMultimap<Identifier, JsonElement> prepared) {
		this.map.clear();

		for(Map.Entry<Identifier, Collection<JsonElement>> entry : prepared.asMap().entrySet()) {
			Identifier identifier = entry.getKey();
			Collection<JsonElement> elements = entry.getValue();

			ArrayList<HashSubstitution<T>> hasher = new ArrayList<>();

			for(JsonElement element : elements) {
				JsonObject o = element.getAsJsonObject();

				if(o.has("replace") && o.getAsJsonPrimitive("replace").getAsBoolean()) {
					hasher.clear();
				}
				hasher.ensureCapacity(hasher.size() + o.size());
				this.compile(o.getAsJsonObject("replacements"), hasher);
			}

			HashSubstitution<T> sub = HashSubstitution.<T>combine().combine(hasher);
			if(this.map.put(identifier, sub) instanceof HasherRef i) {
				i.hasher = sub;
			}
		}
	}

	protected HashSubstitution<T> compile(String key, JsonElement value) {
		if(key.startsWith("#")) {
			return this.substTag(key.substring(1), value);
		} else if(key.startsWith("$")) {
			return this.substRef(key, value);
		} else if(key.indexOf(':') != -1) {
			return this.substRegistry(key, value);
		} else {
			String replacement = value.getAsString();
			return (incoming, val) -> {
				if(key.equals(incoming)) {
					return replacement;
				}
				return incoming;
			};
		}
	}

	protected void compile(JsonObject element, List<HashSubstitution<T>> hashers) {
		for(Map.Entry<String, JsonElement> entry : element.entrySet()) { // this is actually ordered
			hashers.add(this.compile(entry.getKey(), entry.getValue()));
		}
	}

	protected HashSubstitution<T> substTag(String key, JsonElement value) {
		TagManager manager = ServerTagManagerHolder.getTagManager();
		Tag<E> tag = manager.getTag(this.registry, new Identifier(key), null);
		String replacement = value.getAsString();
		return (i, k) -> {
			if(tag.contains(this.getter.apply(k))) {
				return replacement;
			}
			return i;
		};
	}

	protected HashSubstitution<T> substRegistry(String key, JsonElement value) {
		E item = (E) Registry.REGISTRIES.getOrThrow((RegistryKey) this.registry).get(new Identifier(key));
		String replacement = value.getAsString();
		return (incoming, val) -> {
			if(this.getter.apply(val) == item) {
				return replacement;
			}
			return incoming;
		};
	}

	protected HashSubstitution<T> substRef(String key, JsonElement value) {
		return this.forId(new Identifier(key));
	}

	protected HashSubstitution<T> forId(Identifier id) {
		if(id == null) return null;
		return this.map.computeIfAbsent(id, this.func);
	}

	private static class HasherRef<T> implements HashSubstitution<T> {
		private final Map<Identifier, HashSubstitution<T>> map;
		private final Identifier id;
		private HashSubstitution<T> hasher;

		public HasherRef(Map<Identifier, HashSubstitution<T>> map, Identifier id) {
			this.map = map;
			this.id = id;
		}

		@Override
		public String substitute(String incoming, T val) {
			HashSubstitution<T> hash = this.hasher;
			if(hash == null) {
				HashSubstitution<T> substitution = this.map.get(this.id);
				if(substitution == this) {
					substitution = null;
				}
				this.hasher = hash = Validate.notNull(substitution, "no item hash substitute found for id " + this.id);
			}
			return hash.substitute(incoming, val);
		}
	}
}
