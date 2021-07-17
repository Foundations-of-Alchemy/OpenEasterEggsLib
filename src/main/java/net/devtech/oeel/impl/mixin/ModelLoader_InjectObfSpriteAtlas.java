package net.devtech.oeel.impl.mixin;

import java.util.Map;

import com.mojang.datafixers.util.Pair;
import net.devtech.oeel.impl.client.ObfSpriteAtlas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(ModelLoader.class)
public class ModelLoader_InjectObfSpriteAtlas {
	@Shadow @Final private Map<Identifier, Pair<SpriteAtlasTexture, SpriteAtlasTexture.Data>> spriteAtlasData;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
		SpriteAtlasTexture obf = new ObfSpriteAtlas();
		SpriteAtlasTexture.Data data = obf.stitch(resourceManager, null, profiler, i);
		this.spriteAtlasData.put(obf.getId(), Pair.of(obf, data));
	}
}
