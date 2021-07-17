package net.devtech.oeel.impl.client;

import com.google.gson.JsonObject;
import net.devtech.oeel.v0.api.OEEL;

import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfSpriteAtlas {
	public static final Identifier OBF_SPRITE_ATLAS_ID = new Identifier("oeel:obf_atlas");
	public static final MetadataReader READER = new MetadataReader();

	public static final class ObfMetadata {
		public String hash;
		public int width;
		public int height;
	}

	public record ObfEntry(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel, byte[] encryptedData) {}

	public static final class MetadataReader implements ResourceMetadataReader<ObfMetadata> {
		@Override
		public String getKey() {
			return "obf";
		}

		@Override
		public ObfMetadata fromJson(JsonObject json) {
			return OEEL.GSON.fromJson(json, ObfMetadata.class);
		}
	}
}
