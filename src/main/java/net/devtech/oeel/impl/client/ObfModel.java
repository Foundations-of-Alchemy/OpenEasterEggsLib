package net.devtech.oeel.impl.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.hash.HashCode;
import com.mojang.datafixers.util.Pair;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BiHasher;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.OEELHashing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public class ObfModel implements UnbakedModel {
	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public BakedModel bake(ModelLoader loader,
			Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer,
			Identifier modelId) {
		Identifier test = new Identifier("minecraft:block/furnace");
		return new ObfBakedModel(loader.getOrLoadModel(test).bake(loader, textureGetter, rotationContainer, test), textureGetter, loader, rotationContainer);
	}

	public static class ObfBakedModel implements BakedModel, FabricBakedModel {
		private final Function<SpriteIdentifier, Sprite> textureGetter;
		private final ModelLoader loader;
		private final ModelBakeSettings settings;

		public ObfBakedModel(BakedModel defaultModel, Function<SpriteIdentifier, Sprite> getter, ModelLoader loader, ModelBakeSettings settings) {
			this.textureGetter = getter;
			this.loader = loader;
			this.settings = settings;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
			return null;
		}

		@Override
		public boolean useAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean hasDepth() {
			return false;
		}

		@Override
		public boolean isSideLit() {
			return false;
		}

		@Override
		public boolean isBuiltin() {
			return false;
		}

		@Override
		public Sprite getSprite() {
			return null;
		}

		@Override
		public ModelTransformation getTransformation() {
			return null;
		}

		@Override
		public ModelOverrideList getOverrides() {
			return null;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockRenderView blockView,
				BlockState state,
				BlockPos pos,
				Supplier<Random> randomSupplier,
				RenderContext context) {

		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

		}
	}
}
