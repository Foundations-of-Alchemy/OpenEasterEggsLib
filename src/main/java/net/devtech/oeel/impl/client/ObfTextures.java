package net.devtech.oeel.impl.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.v0.api.access.OEELDeserializer;
import net.devtech.oeel.v0.api.data.ObfResourceManager;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfTextures {
	static final Type ATLAS_TYPE = new TypeToken<Map<String, AtlasMeta>>() {}.getType();

	public static List<Key> getMetas(ResourceManager manager) {
		List<Key> newMetas = new ArrayList<>();

		// todo wait why does this need to be in the data dir, this is an easy way of enforcing the need for an assets directory
		ObfResourceManager.findResources(ResourceType.SERVER_DATA, manager, "obf_atlas", s -> s.endsWith(".json")).forEach((identifier, stream) -> {
			try(Reader reader = new InputStreamReader(stream)) {
				Map<String, AtlasMeta> meta = OEELImpl.GSON.fromJson(reader, ATLAS_TYPE);
				String path = identifier.getPath();
				Identifier id = new Identifier(identifier.getNamespace(), path.substring(10, path.length() - 5));
				newMetas.add(new Key(id, meta));
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		});

		return newMetas;
	}

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

		public SpriteMeta(DataInputStream buffer, HashKey key) throws IOException {
			this.offX = buffer.readInt();
			this.offY = buffer.readInt();
			this.width = buffer.readInt();
			this.height = buffer.readInt();
			this.meta = OEELImpl.GSON.fromJson(buffer.readUTF(), JsonObject.class);
			this.data = buffer.readAllBytes();
		}

	}

	public record TotalAtlasSpace(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel) {}

}
