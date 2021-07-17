package net.devtech.oeel.impl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccess {
	@Invoker
	Sprite callLoadSprite(ResourceManager container, Sprite.Info info, int atlasWidth, int atlasHeight, int maxLevel, int x, int y);

	@Accessor
	Map<Identifier, Sprite> getSprites();
}
