package net.devtech.oeel.v0.api.data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.access.JavaHashFunc;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.Reg;
import net.devtech.oeel.v0.api.util.hash.Hasher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class HashFuncComp<T, E> {
	private static final Logger LOGGER = LogManager.getLogger(HashFuncComp.class);
	private final Map<Identifier, HashFunction<T>> map = new HashMap<>();
	private final RegistryKey<Registry<E>> registry;
	private final Function<T, E> getter;
	private final Reg<JavaHashFunc<T>> dynamic;

	public HashFuncComp(RegistryKey<Registry<E>> registry, Function<T, E> getter, Reg<JavaHashFunc<T>> dynamic) {
		this.registry = registry;
		this.getter = getter;
		this.dynamic = dynamic;
	}

	public HashFunction<T> addTo(Identifier id, Iterable<JsonObject> data) {
		ArrayList<HashFunction<T>> hasher = new ArrayList<>();
		for(JsonObject datum : data) {
			if(datum.has("replace") && datum.getAsJsonPrimitive("replace").getAsBoolean()) {
				hasher.clear();
			}
			hasher.ensureCapacity(hasher.size() + datum.size());
			this.compile(datum.getAsJsonObject("functions"), hasher);
		}

		HashFunction<T> sub = HashFunction.<T>combine().combine(hasher);
		if(this.map.put(id, sub) instanceof HasherRef<T> i) {
			i.hasher = sub;
		}
		return sub;
	}

	public void clear() {
		this.map.clear();
	}

	public Map<Identifier, HashFunction<T>> getMap() {
		return this.map;
	}

	public Set<Map.Entry<Identifier, HashFunction<T>>> entrySet() {
		return map.entrySet();
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
			throw new UnsupportedOperationException("unknown encryptionKey " + key);
		}
	}

	protected void compile(JsonObject element, List<HashFunction<T>> hashers) {
		for(Map.Entry<String, JsonElement> entry : element.entrySet()) { // this is actually ordered
			hashers.add(this.compile(entry.getKey(), entry.getValue()));
		}
	}

	protected HashFunction<T> dynamic(String key, JsonElement value) {
		JavaHashFunc<T> item = this.dynamic.get(new Identifier(key));
		if(item == null) {
			throw new IllegalArgumentException("no dynamic hash function found for " + key);
		}
		return item.apply(value);
	}

	static class Ex extends Exception {
		Ex(String message) {
			super(message);
		}
	}

	protected HashFunction<T> tag(String key, JsonElement value) {
		TagManager manager = ServerTagManagerHolder.getTagManager();
		try {
			Tag<E> tag = manager.getTag(this.registry, new Identifier(key), identifier -> new Ex(identifier.toString()));
			String val = value.getAsString();
			byte[] replacement = val.getBytes(StandardCharsets.UTF_8);
			return (i, k) -> {
				if(tag.contains(this.getter.apply(k))) {
					i.putBytes(replacement);
				}
			};
		} catch(Ex ex) {
			return (i, k) -> {};
		}
	}

	protected HashFunction<T> registry(String key, JsonElement value) {
		E item = (E) Registry.REGISTRIES.getOrThrow((RegistryKey) this.registry).get(new Identifier(key));
		String val = value.getAsString();
		byte[] replacement = val.getBytes(StandardCharsets.UTF_8);
		return (i, k) -> {
			if(this.getter.apply(k) == item) {
				i.putBytes(replacement);
			}
		};
	}

	protected HashFunction<T> hasher(String key, JsonElement value) {
		E item = (E) Registry.REGISTRIES.getOrThrow((RegistryKey) this.registry).get(new Identifier(key));
		String val = value.getAsString();
		byte[] replacement = val.getBytes(StandardCharsets.UTF_8);
		return (i, k) -> {
			if(this.getter.apply(k) == item) {
				i.putBytes(replacement);
			}
		};
	}

	protected HashFunction<T> ref(String key, JsonElement value) {
		return this.forId(new Identifier(key));
	}

	public HashFunction<T> forId(Identifier id) {
		if(id == null) return null;
		return this.map.computeIfAbsent(id, id1 -> new HasherRef<>(map, id1));
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
				HashFunction<T> function = map.get(this.id);
				if(function == this) {
					function = null;
				}
				this.hasher = hash = Validate.notNull(function, "no item hasher found for id " + this.id);
			}
			hash.hash(hasher, val);
		}
	}
}
