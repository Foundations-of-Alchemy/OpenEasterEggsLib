package net.devtech.oeel.v0.api;

import java.util.function.Function;

import net.devtech.oeel.impl.mixin.ModelLoaderAccess;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

/**
 * makes the 3d flat item models.
 * You know how all the item textures are 2d, but when u hold them in ur hand they're somehow 3d (1 pixel depth)? Yea this does that
 */
public final class ItemModelFactory {
	private ItemModelFactory() {}

	public static JsonUnbakedModel createModel(Function<SpriteIdentifier, Sprite> textureGetter, Identifier sprite) {
		JsonUnbakedModel model = JsonUnbakedModel.deserialize(String.format("{\"parent\": \"minecraft:item/generated\",\"textures\": {\"layer0\": \"%s\"}}", sprite));
		return ModelLoaderAccess.getITEM_MODEL_GENERATOR().create(textureGetter, model);
	}
}
