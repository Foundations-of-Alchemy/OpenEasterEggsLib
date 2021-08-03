package yeet;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import client.AbstractBakedModel;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.ItemModelFactory;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.HashId;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import client.OEELModelProvider;

import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public class AClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(r -> new OEELModelProvider());
		HashKey key = new HashKey(-1L, -1L, -1L, -1L);
		EncryptionEntry entry = new EncryptionEntry(key, key.toByteArray());
		Identifier id = HashId.create(new Identifier("oeelt:default"), entry);
		var spriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, id);

		OEELModelProvider.add(new AbstractBakedModel(spriteId) {
			ModelLoader loader;
			Function<SpriteIdentifier, Sprite> textureGetter;
			ModelBakeSettings rotationContainer;
			Identifier modelId;

			@Override
			public void emitItemQuads(Mesh mesh, ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
				try {
					JsonUnbakedModel model = ItemModelFactory.createModel(this.textureGetter, id);
					context.fallbackConsumer().accept(model.bake(this.loader, this.textureGetter, this.rotationContainer, this.modelId));
				} catch(Exception e) {
					e.printStackTrace();
					throw Validate.rethrow(e);
				}
			}

			static final Identifier PARENT = new Identifier("minecraft:item/generated");

			@Override
			protected Identifier getItemTransformationParentId() {
				return PARENT;
			}

			@Override
			protected boolean build(Renderer renderer,
					QuadEmitter emitter,
					ModelLoader loader,
					Function<SpriteIdentifier, Sprite> textureGetter,
					ModelBakeSettings rotationContainer,
					Identifier modelId) {
				this.loader = loader;
				this.textureGetter = textureGetter;
				this.rotationContainer = rotationContainer;
				this.modelId = modelId;
				return false;
			}
		}, "item/test_item");

		//OEELModelProvider.add(new CubeModel(spriteId, CubeData.withAll(spriteId)), "item/test_item");
	}
}
