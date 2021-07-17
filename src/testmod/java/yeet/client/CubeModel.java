package yeet.client;

import java.util.function.Function;

import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class CubeModel extends AbstractBakedModel {
	public final CubeData data;
	public CubeModel(SpriteIdentifier particles, CubeData data) {
		super(particles);
		this.data = data;
	}

	@Override
	protected boolean build(Renderer renderer,
			QuadEmitter emitter,
			ModelLoader loader,
			Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer,
			Identifier modelId) {
		AbstractBakedModel.buildCube(rotationContainer, emitter, textureGetter, this.data);
		return true;
	}
}
