package net.devtech.oeel.impl.mixin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.client.ObfTextures;
import net.devtech.oeel.impl.client.ResourceManagerHack;
import net.devtech.oeel.v0.api.data.ObfResourceManager;
import net.devtech.oeel.v0.api.util.HashId;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(SpriteAtlasTexture.class)
abstract class SpriteAtlasTextureMixin_ObfSupport extends AbstractTexture {
	final Map<Sprite.Info, ObfTextures.Key> oeel_encryptedSpriteData = new HashMap<>();
	final Map<Identifier, ObfTextures.TotalAtlasSpace> oeel_atlasSpace = new HashMap<>();
	int mipmapLevel;

	@Shadow @Final private List<TextureTickListener> animatedSprites;
	@Shadow @Final private Identifier id;
	@Shadow @Final private Map<Identifier, Sprite> sprites;

	@Inject(method = "stitch",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureStitcher;stitch()V"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStitch(ResourceManager manager,
			Stream<Identifier> idStream,
			Profiler profiler,
			int mipmapLevel,
			CallbackInfoReturnable<SpriteAtlasTexture.Data> cir,
			Set<Identifier> set,
			int i,
			TextureStitcher stitcher,
			int mimapLevel) {
		this.mipmapLevel = mipmapLevel;
		this.oeel_encryptedSpriteData.clear();
		for(ObfTextures.Key meta : ObfTextures.getMetas(manager)) {
			ObfTextures.AtlasMeta atlasMeta = meta.meta().get(this.id.toString());
			if(atlasMeta != null) {
				Sprite.Info info = new Sprite.Info(meta.id(), atlasMeta.atlasWidth, atlasMeta.atlasHeight, AnimationResourceMetadata.EMPTY);
				this.oeel_encryptedSpriteData.put(info, meta);
				stitcher.add(info);
			}
		}
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_24105",
			at = @At(value = "INVOKE",
					target =
							"Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)" + "Ljava/util" + "/concurrent/CompletableFuture;"),
			cancellable = true)
	public void stitch(int i,
			Queue queue,
			List list,
			ResourceManager manager,
			Sprite.Info info,
			int atlasWidth,
			int atlasHeight,
			int x,
			int y,
			CallbackInfo ci) {
		ObfTextures.Key atlasMeta = this.oeel_encryptedSpriteData.remove(info);
		if(atlasMeta != null) {
			var val = new ObfTextures.TotalAtlasSpace(info, atlasWidth, atlasHeight, x, y, this.mipmapLevel);
			this.oeel_atlasSpace.put(atlasMeta.id(), val);
			ci.cancel();
		}
	}

	@ModifyVariable(method = "getSprite", at = @At(value = "STORE"))
	public Sprite postAssign(Sprite sprite, Identifier spriteId) {
		if(sprite != null) {
			return sprite;
		}
		outer:
		try {
			String path = spriteId.getPath();
			int atlasIndex = path.indexOf("/oeel/");
			if(atlasIndex == -1) break outer;
			Identifier atlasId = new Identifier(spriteId.getNamespace(), path.substring(0, atlasIndex));
			ObfTextures.TotalAtlasSpace space = this.oeel_atlasSpace.get(atlasId);
			HashId id = HashId.getKey(spriteId);
			if(id == null) break outer;
			if(space == null) break outer;
			Sprite unencryptSprite = this.oeel_unencryptSprite(spriteId, id.validation, id.encryption, space);
			this.sprites.put(spriteId, unencryptSprite);
			return unencryptSprite;
		} catch(GeneralSecurityException | IOException e) {
			e.printStackTrace();
			throw Validate.rethrow(e);
		}
		return null;
	}

	@Shadow
	@Nullable
	protected abstract Sprite loadSprite(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y);

	private Sprite oeel_unencryptSprite(Identifier id, HashKey validationKey, byte[] encryptionKey, ObfTextures.TotalAtlasSpace space)
			throws GeneralSecurityException, IOException {
		var decrypt = ObfResourceManager.client.decryptOnce(validationKey, encryptionKey, ObfTextures.SpriteMeta::new);
		var only = Iterables.getOnlyElement(decrypt);
		ResourceManagerHack hack = new ResourceManagerHack(only.data);
		this.bindTexture();
		var reader = AnimationResourceMetadata.READER;
		AnimationResourceMetadata metadata;
		if(only.meta != null && only.meta.has(reader.getKey())) {
			metadata = reader.fromJson(only.meta.getAsJsonObject(reader.getKey()));
		} else {
			metadata = AnimationResourceMetadata.EMPTY;
		}
		Sprite.Info info = new Sprite.Info(id, only.width, only.height, metadata);
		var s = this.loadSprite(hack, info, space.atlasWidth(), space.atlasHeight(), space.maxLevel(), space.x() + only.offX, space.y() + only.offY);
		s.upload();
		TextureTickListener textureTickListener = s.getAnimation();
		if(textureTickListener != null) {
			this.animatedSprites.add(textureTickListener);
		}

		return s;
	}
}
