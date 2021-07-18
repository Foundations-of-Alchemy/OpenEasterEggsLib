package net.devtech.oeel.impl.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.OEEL;

import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfTextures {
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

	public record AtlasSpace(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel) {}
}
