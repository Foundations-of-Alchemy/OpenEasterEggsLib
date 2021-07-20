package net.devtech.oeel.impl.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfTextures {
	public static final SpriteDataDeserializer DESERIALIZER = new SpriteDataDeserializer();
	static List<Key> metas;
	static WeakReference<ResourceManager> manager;

	static final Type ATLAS_TYPE = new TypeToken<Map<String, AtlasMeta>>() {}.getType();

	public record Key(Identifier id, Map<String, AtlasMeta> meta) {}


	public static List<Key> getMetas(ResourceManager manager) throws IOException {
		if(metas != null && ObfTextures.manager.get() == manager) {
			return metas;
		} else {
			List<Key> metas = new ArrayList<>();
			for(Identifier resourceId : manager.findResources("obf_atlas", s -> s.endsWith(".json"))) {
				Resource resource = manager.getResource(resourceId);
				try(Reader reader = new InputStreamReader(resource.getInputStream())) {
					Map<String, AtlasMeta> meta = OEEL.GSON.fromJson(reader, ATLAS_TYPE);
					String path = resourceId.getPath();
					Identifier id = new Identifier(resourceId.getNamespace(), path.substring(9, path.length()-5));
					metas.add(new Key(id, meta));
				} catch(IOException e) {
					throw Validate.rethrow(e);
				}
			}
			ObfTextures.manager = new WeakReference<>(manager);
			ObfTextures.metas = metas;
			return metas;
		}
	}

	public static final class AtlasMeta {
		public int atlasWidth;
		public int atlasHeight;
	}

	public static final class SpriteMeta {
		public int offX;
		public int offY;
		public byte[] data;
	}

	public record AtlasSpace(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel) {}

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
		public void read(SpriteMeta instance, ByteBuffer buffer, HashKey inputHash) {
			instance.offX = buffer.getInt();
			instance.offY = buffer.getInt();
			int len = buffer.getInt();
			byte[] data = new byte[len];
			buffer.get(data);
		}
	}
}
