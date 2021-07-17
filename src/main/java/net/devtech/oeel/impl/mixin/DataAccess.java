package net.devtech.oeel.impl.mixin;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasTexture.Data.class)
public interface DataAccess {
	@Accessor
	Set<Identifier> getSpriteIds();

	@Accessor
	int getWidth();

	@Accessor
	int getHeight();

	@Accessor
	int getMaxLevel();

	@Accessor
	List<Sprite> getSprites();
}
