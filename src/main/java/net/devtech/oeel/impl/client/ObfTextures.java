package net.devtech.oeel.impl.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.util.func.Iter;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfTextures {
	public static final SpriteDataDeserializer DESERIALIZER = new SpriteDataDeserializer();
	static final WeakHashMap<ResourceManager, List<Key>> METAS = new WeakHashMap<>(1);
	static final Type ATLAS_TYPE = new TypeToken<Map<String, AtlasMeta>>() {}.getType();

	public static List<Key> getMetas(ResourceManager manager) throws IOException {
		List<Key> metas = METAS.get(manager);
		if(metas == null) {
			METAS.clear();
			List<Key> newMetas = new ArrayList<>();

			findResources(ResourceType.SERVER_DATA, manager, "obf_atlas", s -> s.endsWith(".json")).forEach((identifier, stream) -> {
				try(Reader reader = new InputStreamReader(stream)) {
					Map<String, AtlasMeta> meta = OEELInternal.GSON.fromJson(reader, ATLAS_TYPE);
					String path = identifier.getPath();
					Identifier id = new Identifier(identifier.getNamespace(), path.substring(9, path.length() - 5));
					newMetas.add(new Key(id, meta));
				} catch(IOException e) {
					throw Validate.rethrow(e);
				}
			});

			METAS.put(manager, newMetas);
			return newMetas;
		}
		return metas;
	}

	static Map<Identifier, InputStream> findResources(ResourceType type, ResourceManager manager, String startingPath, Predicate<String> pathPredicate)
			throws IOException {
		Map<Identifier, InputStream> map = new HashMap<>();
		for(ResourcePack pack : Iter.iter(manager::streamResourcePacks)) {
			for(String namespace : pack.getNamespaces(type)) {
				for(Identifier resource : pack.findResources(type, namespace, startingPath, Integer.MAX_VALUE, pathPredicate)) {
					map.put(resource, pack.open(type, resource));
				}
			}
		}
		return map;
	}

	record Resource(Identifier id, InputStream stream) {}

	public record Key(Identifier id, Map<String, AtlasMeta> meta) {}

	public static final class AtlasMeta {
		public int atlasWidth;
		public int atlasHeight;
	}

	public static final class SpriteMeta {
		public int offX;
		public int offY;
		public int width;
		public int height;
		public JsonObject meta;
		public byte[] data;
	}

	public record TotalAtlasSpace(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel) {}

	public static class SpriteDataDeserializer implements ByteDeserializer<SpriteMeta> {
		@Override
		public String magic() {
			return "oeel:tex";
		}

		@Override
		public SpriteMeta newInstance() {
			return new SpriteMeta();
		}

		@Override
		public void read(SpriteMeta instance, DataInputStream buffer, HashKey inputHash) throws IOException {
			instance.offX = buffer.readInt();
			instance.offY = buffer.readInt();
			instance.width = buffer.readInt();
			instance.height = buffer.readInt();
			instance.meta = OEELInternal.GSON.fromJson(buffer.readUTF(), JsonObject.class);
			instance.data = buffer.readAllBytes();
		}
	}
}
