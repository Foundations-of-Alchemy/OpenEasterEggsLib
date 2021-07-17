package net.devtech.oeel.impl.mixin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.hash.HashCode;
import com.mojang.datafixers.util.Pair;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.client.ObfSpriteAtlas;
import net.devtech.oeel.impl.client.ResourceManagerHack;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(SpriteAtlasTexture.class)
public abstract class SpriteAtlasTextureMixin_ObfSupport extends AbstractTexture {
	final Map<String, ObfSpriteAtlas.ObfEntry> oeel_encryptedSprites = new HashMap<>();
	final Map<Sprite.Info, Pair<String, byte[]>> oeel_encryptedSpriteData = new HashMap<>();
	int mipmapLevel;

	@Shadow @Final private List<TextureTickListener> animatedSprites;

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
		this.oeel_encryptedSprites.clear();
		this.oeel_encryptedSpriteData.clear();

		// todo fix paths, maybe, idk, perhaps
		for(Identifier sprite : manager.findResources("obf_sprite", s -> s.endsWith(".data"))) {
			try(Resource resource = manager.getResource(sprite)) {
				ObfSpriteAtlas.ObfMetadata obfMeta = resource.getMetadata(ObfSpriteAtlas.READER);
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
				byte[] bytes = resource.getInputStream().readAllBytes();
				this.oeel_encryptedSpriteData.put(info, Pair.of(obfMeta.hash, bytes));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Inject(method = "method_24105",
			at = @At(value = "INVOKE",
					target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)" + "Ljava/util" +
					         "/concurrent/CompletableFuture;"),
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
		Pair<String, byte[]> pair = this.oeel_encryptedSpriteData.get(info);
		if(pair != null) {
			var val = new ObfSpriteAtlas.ObfEntry(info, atlasWidth, atlasHeight, x, y, this.mipmapLevel, pair.getSecond());
			this.oeel_encryptedSprites.put(pair.getFirst(), val);
			ci.cancel();
		}
	}


	@Redirect(method = "getSprite", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	public Object getObfSprite(Map map, Object key, Identifier spriteId) {
		try {
			HashCode code = HashCode.fromString(spriteId.getNamespace());
			ObfSpriteAtlas.ObfEntry entry = this.oeel_encryptedSprites.remove(code);
			Sprite sprite = this.oeel_unencryptSprite(spriteId, entry);
			map.put(spriteId, sprite);
			return sprite;
		} catch(IllegalArgumentException | NullPointerException e) {
			return map.get(key);
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	@Shadow
	@Nullable
	protected abstract Sprite loadSprite(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y);

	private Sprite oeel_unencryptSprite(Identifier id, ObfSpriteAtlas.ObfEntry entry) throws GeneralSecurityException, IOException {
		HashCode encryptionKey = HashCode.fromString(id.getPath());
		byte[] decryptedBytes = OEELEncrypting.decrypt(encryptionKey, entry.encryptedData());
		var hack = new ResourceManagerHack(decryptedBytes);
		this.bindTexture();
		Sprite sprite = this.loadSprite(hack, entry.info(), entry.atlasWidth(), entry.atlasHeight(), entry.maxLevel(), entry.x(), entry.y());
		sprite.upload();
		TextureTickListener textureTickListener = sprite.getAnimation();
		if(textureTickListener != null) {
			this.animatedSprites.add(textureTickListener);
		}

		return sprite;
	}
}
