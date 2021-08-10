package net.devtech.oeel.v0.api.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

/**
 * non-obfuscated texture dependencies must be declared because otherwise they wont be added to the atlas
 */
public class OEELUnbakedModel implements UnbakedModel {
	final Collection<SpriteIdentifier> textureDependencies;
	Collection<SpriteIdentifier> resolvedTextureDependencies;
	final Collection<Identifier> modelDependencies;

	public OEELUnbakedModel() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	public OEELUnbakedModel(Collection<SpriteIdentifier> textureDependencies) {
		this(textureDependencies, Collections.emptyList());
	}

	public OEELUnbakedModel(Collection<SpriteIdentifier> textureDependencies, Collection<Identifier> modelDependencies) {
		this.textureDependencies = textureDependencies;
		this.modelDependencies = modelDependencies;
	}

	/**
	 * used for loading dependencies, as long as *a* model contains the deps u should be alright.
	 */
	@Override
	public Collection<Identifier> getModelDependencies() {
		return this.modelDependencies;
	}

	/**
	 * Since assets are supposed to be decrypted anyways, we don't necessarily need to pass this in
	 */
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		var deps = this.resolvedTextureDependencies;
		if(deps == null) {
			List<SpriteIdentifier> resolved = new ArrayList<>(this.textureDependencies);
			for(Identifier dependency : this.getModelDependencies()) {
				UnbakedModel model = unbakedModelGetter.apply(dependency);
				resolved.addAll(model.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
			}
			this.resolvedTextureDependencies = deps = resolved;
		}
		return deps;
	}

	@Nullable
	@Override
	public BakedModel bake(ModelLoader loader,
			Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer,
			Identifier modelId) {
		return null;
	}
}
