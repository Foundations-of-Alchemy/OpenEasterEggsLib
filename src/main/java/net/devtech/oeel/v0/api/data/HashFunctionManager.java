package net.devtech.oeel.v0.api.data;

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
import net.devtech.oeel.v0.api.access.DynamicHashFunction;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.hash.Hasher;

import net.minecraft.entity.Entity;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

@SuppressWarnings("unchecked")
public class HashFunctionManager<T, E> extends MultiJsonDataLoader<JsonElement> {
	public static final Map<Identifier, HashFunction<ItemKey>> ITEM_HASH_FUNCTIONS = new HashMap<>();
	public static final Map<Identifier, HashFunction<Entity>> ENTITY_HASH_FUNCTIONS = new HashMap<>();
	public static final Map<Identifier, HashFunction<BlockData>> BLOCK_HASH_FUNCTIONS = new HashMap<>();

	private final Map<Identifier, HashFunction<T>> map;
	private final Function<Identifier, HashFunction<T>> func;
	private final RegistryKey<Registry<E>> registry;
	private final Function<T, E> getter;
	private final Registry<DynamicHashFunction<T>> dynamic;

	public HashFunctionManager(String dataType,
			Map<Identifier, HashFunction<T>> map,
			RegistryKey<Registry<E>> registry,
			Function<T, E> getter, Registry<DynamicHashFunction<T>> dynamic) {
		super(dataType, JsonElement.class);
		this.map = map;
		this.registry = registry;
		this.getter = getter;
		this.dynamic = dynamic;
		this.func = id1 -> new HasherRef<>(this.map, id1);
	}

	public static HashFunction<ItemKey> item(Identifier funcId) {
		return ITEM_HASH_FUNCTIONS.computeIfAbsent(funcId, id -> new HasherRef<>(ITEM_HASH_FUNCTIONS, id));
	}

	public static HashFunction<Entity> entity(Identifier funcId) {
		return ENTITY_HASH_FUNCTIONS.computeIfAbsent(funcId, id -> new HasherRef<>(ENTITY_HASH_FUNCTIONS, id));
	}

	public static HashFunction<BlockData> block(Identifier funcId) {
		return BLOCK_HASH_FUNCTIONS.computeIfAbsent(funcId, id -> new HasherRef<>(BLOCK_HASH_FUNCTIONS, id));
	}

	@Override
	protected void apply(ListMultimap<Identifier, JsonElement> prepared) {
		this.map.clear();

		for(Map.Entry<Identifier, Collection<JsonElement>> entry : prepared.asMap().entrySet()) {
			Identifier identifier = entry.getKey();
			Collection<JsonElement> elements = entry.getValue();

			ArrayList<HashFunction<T>> hasher = new ArrayList<>();

			for(JsonElement element : elements) {
				JsonObject o = element.getAsJsonObject();

				if(o.has("replace") && o.getAsJsonPrimitive("replace").getAsBoolean()) {
					hasher.clear();
				}
				hasher.ensureCapacity(hasher.size() + o.size());
				this.compile(o.getAsJsonObject("replacements"), hasher);
			}

			HashFunction<T> sub = HashFunction.<T>combine().combine(hasher);
			if(this.map.put(identifier, sub) instanceof HasherRef i) {
				i.hasher = sub;
			}
		}
	}

	protected HashFunction<T> compile(String key, JsonElement value) {
		if(key.startsWith("#")) {
			return this.tag(key.substring(1), value);
		} else if(key.startsWith("$")) {
			return this.ref(key.substring(1), value);
		} else if(key.startsWith("&")) {
			return this.dynamic(key.substring(1), value);
		} else if(key.indexOf(':') != -1) {
			return this.registry(key, value);
		} else {
			throw new UnsupportedOperationException("unknown key " + key);
		}
	}

	protected void compile(JsonObject element, List<HashFunction<T>> hashers) {
		for(Map.Entry<String, JsonElement> entry : element.entrySet()) { // this is actually ordered
			hashers.add(this.compile(entry.getKey(), entry.getValue()));
		}
	}

	protected HashFunction<T> dynamic(String key, JsonElement value) {
		DynamicHashFunction<T> item = this.dynamic.get(new Identifier(key));
		if(item == null) {
			throw new IllegalArgumentException("no dynamic hash function found for " + key);
		}
		return item.apply(value);
	}

	protected HashFunction<T> tag(String key, JsonElement value) {
		TagManager manager = ServerTagManagerHolder.getTagManager();
		Tag<E> tag = manager.getTag(this.registry, new Identifier(key), null);
		byte[] replacement = OEELEncrypting.decodeBase16(value.getAsString());
		return (i, k) -> {
			if(tag.contains(this.getter.apply(k))) {
				i.putBytes(replacement);
			}
		};
	}

	protected HashFunction<T> registry(String key, JsonElement value) {
		E item = (E) Registry.REGISTRIES.getOrThrow((RegistryKey) this.registry).get(new Identifier(key));
		byte[] replacement = OEELEncrypting.decodeBase16(value.getAsString());
		return (incoming, val) -> {
			if(this.getter.apply(val) == item) {
				incoming.putBytes(replacement);
			}
		};
	}

	protected HashFunction<T> hasher(String key, JsonElement value) {
		E item = (E) Registry.REGISTRIES.getOrThrow((RegistryKey) this.registry).get(new Identifier(key));
		byte[] replacement = OEELEncrypting.decodeBase16(value.getAsString());
		return (incoming, val) -> {
			if(this.getter.apply(val) == item) {
				incoming.putBytes(replacement);
			}
		};
	}

	protected HashFunction<T> ref(String key, JsonElement value) {
		return this.forId(new Identifier(key));
	}

	protected HashFunction<T> forId(Identifier id) {
		if(id == null) return null;
		return this.map.computeIfAbsent(id, this.func);
	}

	private static class HasherRef<T> implements HashFunction<T> {
		private final Map<Identifier, HashFunction<T>> map;
		private final Identifier id;
		private HashFunction<T> hasher;

		public HasherRef(Map<Identifier, HashFunction<T>> map, Identifier id) {
			this.map = map;
			this.id = id;
		}

		@Override
		public void hash(Hasher hasher, T val) {
			HashFunction<T> hash = this.hasher;
			if(hash == null) {
				HashFunction<T> function = this.map.get(this.id);
				if(function == this) {
					function = null;
				}
				this.hasher = hash = Validate.notNull(function, "no item hasher found for id " + this.id);
			}
			hash.hash(hasher, val);
		}
	}
}
