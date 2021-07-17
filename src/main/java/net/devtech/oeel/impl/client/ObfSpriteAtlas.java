package net.devtech.oeel.impl.client;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.hash.HashCode;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.mixin.DataAccess;
import net.devtech.oeel.impl.mixin.SpriteAtlasTextureAccess;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.util.OEELEncrypting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

/**
 * sprite atlas texture who's textures are encrypted and are lazily decrypted. To get a texture the sprite identifier is [oeel:obf_atlas,
 * <validation_key><encryption_key>]
 */
public class ObfSpriteAtlas extends SpriteAtlasTexture {
	public static final Identifier OBF_SPRITE_ATLAS_ID = new Identifier("oeel:obf_atlas");
	public static final MetadataReader READER = new MetadataReader();
	protected final int maxSize = RenderSystem.maxSupportedTextureSize();
	final Map<HashCode, ObfEntry> encryptedSprites = new HashMap<>();
	MissingSprite missingSprite;

	public ObfSpriteAtlas() {
		super(OBF_SPRITE_ATLAS_ID);
	}

	@Override
	public Data stitch(ResourceManager manager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel) {
		MinecraftClient client = MinecraftClient.getInstance();
		TextureStitcher stitcher = new TextureStitcher(this.maxSize, this.maxSize, client.options.mipmapLevels);

		Map<Sprite.Info, Pair<HashCode, byte[]>> hashCodes = new HashMap<>();
		for(Identifier sprite : manager.findResources("obf_sprite", s -> s.endsWith(".data"))) {
			try(Resource resource = manager.getResource(sprite)) {
				ObfMetadata obfMeta = resource.getMetadata(READER);
				if(obfMeta == null) {
					throw new IllegalStateException("\"obf\" entry must exist with x, y & info! " + sprite);
				}

				AnimationResourceMetadata animMeta = resource.getMetadata(AnimationResourceMetadata.READER);
				if(animMeta == null) {
					animMeta = AnimationResourceMetadata.EMPTY;
				}

				Pair<Integer, Integer> pair = animMeta.ensureImageSize(obfMeta.width, obfMeta.height);
				Sprite.Info info = new Sprite.Info(sprite, pair.getFirst(), pair.getSecond(), animMeta);
				stitcher.add(info);
				hashCodes.put(info, Pair.of(HashCode.fromString(obfMeta.hash), resource.getInputStream().readAllBytes()));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		stitcher.add(MissingSprite.getMissingInfo());
		stitcher.stitch();

		this.encryptedSprites.clear();
		stitcher.getStitchedSprites((info, atlasWidth, atlasHeight, x, y) -> {
			if(MissingSprite.getMissingInfo() != info) {
				Pair<HashCode, byte[]> pair = hashCodes.get(info);
				this.encryptedSprites.put(pair.getFirst(), new ObfEntry(info, atlasWidth, atlasHeight, x, y, mipmapLevel, pair.getSecond()));
			} else {
				MissingSprite missingSprite = MissingSprite.getMissingSprite(this, mipmapLevel, atlasWidth, atlasHeight, x, y);
				Map<Identifier, Sprite> spriteMap = ((SpriteAtlasTextureAccess) this).getSprites();
				spriteMap.put(MissingSprite.getMissingSpriteId(), missingSprite);
				this.missingSprite = missingSprite;
			}
		});
		return new SpriteAtlasTexture.Data(Collections.emptySet(), stitcher.getWidth(), stitcher.getHeight(), mipmapLevel, Collections.emptyList());
	}

	@Override
	public Sprite getSprite(Identifier id) {
		Map<Identifier, Sprite> spriteMap = ((SpriteAtlasTextureAccess) this).getSprites();
		return spriteMap.computeIfAbsent(id, i -> {
			try {
				HashCode code = HashCode.fromString(i.getNamespace());
				ObfEntry entry = this.encryptedSprites.remove(code);
				return this.unencryptSprite(i, entry);
			} catch(IllegalArgumentException | NullPointerException e) {
				return this.missingSprite;
			} catch(GeneralSecurityException | IOException e) {
				throw Validate.rethrow(e);
			}
		});
	}

	private Sprite unencryptSprite(Identifier id, ObfEntry entry) throws GeneralSecurityException, IOException {
		HashCode encryptionKey = HashCode.fromString(id.getPath());
		byte[] decryptedBytes = OEELEncrypting.decrypt(encryptionKey, entry.encryptedData());
		SpriteAtlasTextureAccess access = (SpriteAtlasTextureAccess) this;
		var hack = new ResourceManagerHack(decryptedBytes);
		Sprite sprite = access.callLoadSprite(hack, entry.info, entry.atlasWidth, entry.atlasHeight, entry.maxLevel, entry.x, entry.y);
		this.bindTexture();
		sprite.upload();
		TextureTickListener textureTickListener = sprite.getAnimation();
		if(textureTickListener != null) {
			((SpriteAtlasTextureAccess) this).getAnimatedSprites().add(textureTickListener);
		}

		return sprite;
	}

	static final class ObfMetadata {
		String hash;
		int width;
		int height;
	}

	record ObfEntry(Sprite.Info info, int atlasWidth, int atlasHeight, int x, int y, int maxLevel, byte[] encryptedData) {}

	static final class MetadataReader implements ResourceMetadataReader<ObfMetadata> {
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
