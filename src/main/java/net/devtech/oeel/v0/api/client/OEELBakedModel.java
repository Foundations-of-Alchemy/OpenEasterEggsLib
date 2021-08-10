package net.devtech.oeel.v0.api.client;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
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
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

final class OEELBakedModel implements BakedModel, FabricBakedModel {
	public final OEELUnbakedModel model;
	public final ModelLoader loader;
	public final Function<SpriteIdentifier, Sprite> textureGetter;
	public final ModelBakeSettings rotationContainer;
	public final Identifier modelId;
	final ModelOverrideList list = new OEELModelOverrideList(this);

	OEELBakedModel(OEELUnbakedModel model,
			ModelLoader loader,
			Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer,
			Identifier modelId) {
		this.model = model;
		this.loader = loader;
		this.textureGetter = textureGetter;
		this.rotationContainer = rotationContainer;
		this.modelId = modelId;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return this.list;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return Collections.emptyList();
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
	public Sprite getParticleSprite() {
		return null;
	}

	@Override
	public ModelTransformation getTransformation() {
		return ModelTransformation.NONE;
	}
}
