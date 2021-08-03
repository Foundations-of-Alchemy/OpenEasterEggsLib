package client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import io.github.astrarre.util.v0.api.Lazy;
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

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public abstract class AbstractBakedModel implements UnbakedModel {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());
	public static final Lazy<Renderer> RENDERER = Lazy.of(RendererAccess.INSTANCE::getRenderer);
	public static final Lazy<RenderMaterial> EMISSIVE = RENDERER.map(Renderer::materialFinder)
			                                                    .map(m -> m.emissive(0, true))
			                                                    .map(MaterialFinder::find);
	public static final Lazy<RenderMaterial> EMISSIVE_LAYER = RENDERER.map(Renderer::materialFinder)
			                                                          .map(m -> m.emissive(0, true))
			                                                          .map(r -> r.blendMode(0, BlendMode.TRANSLUCENT))
			                                                          .map(MaterialFinder::find);
	public static final Lazy<RenderMaterial> TRANSPARENT = RENDERER.map(Renderer::materialFinder)
			                                                       .map(r -> r.blendMode(0, BlendMode.TRANSLUCENT))
			                                                       .map(MaterialFinder::find);
	private static final Identifier DEFAULT_BLOCK_MODEL = new Identifier("minecraft:block/block");
	protected final Map<SpriteIdentifier, Sprite> resolved = new ConcurrentHashMap<>(); // todo clear
	protected final Set<SpriteIdentifier> textureDependencies = new HashSet<>();
	protected final SpriteIdentifier particles;
	private ModelTransformation itemTransformation;

	protected AbstractBakedModel(SpriteIdentifier particles) {
		this.particles = particles;
		this.textureDependencies.add(particles);
	}

	public void emitBlockQuads(Mesh mesh,
			BlockRenderView blockView,
			BlockState state,
			BlockPos pos,
			Supplier<Random> randomSupplier,
			RenderContext context) {
		if(mesh != null) {
			context.meshConsumer().accept(mesh);
		}
	}

	public void emitItemQuads(Mesh mesh, ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		if(mesh != null) {
			context.meshConsumer().accept(mesh);
		}
	}

	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return null;
	}

	public boolean useAmbientOcclusion() {
		return true;
	}

	public boolean hasDepth() {
		return false;
	}

	public boolean isSideLit() {
		return true;
	}

	public boolean isBuiltin() {
		return false;
	}

	public Sprite getParticleSprite() {
		return resolved.get(this.particles);
	}

	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

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
		JsonUnbakedModel defaultBlockModel = (JsonUnbakedModel) loader.getOrLoadModel(this.getItemTransformationParentId());
		this.itemTransformation = defaultBlockModel.getTransformations();
		MeshBuilder builder = RENDERER.get().meshBuilder();
		boolean builtMesh = this.build(RENDERER.get(), builder.getEmitter(), loader, textureGetter, rotationContainer, modelId);
		if(builtMesh) {
			Mesh mesh = builder.build();
			return new Baked(mesh);
		} else {
			return new Meshless();
		}
	}

	protected Identifier getItemTransformationParentId() {
		return DEFAULT_BLOCK_MODEL;
	}

	protected abstract boolean build(Renderer renderer,
			QuadEmitter emitter,
			ModelLoader loader,
			Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer,
			Identifier modelId);

	public class Meshless implements BakedModel, FabricBakedModel {
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
			AbstractBakedModel.this.emitBlockQuads(null, blockView, state, pos, randomSupplier, context);
		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			AbstractBakedModel.this.emitItemQuads(null, stack, randomSupplier, context);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
			return Collections.emptyList();
		}

		@Override
		public boolean useAmbientOcclusion() {
			return AbstractBakedModel.this.useAmbientOcclusion();
		}

		@Override
		public boolean hasDepth() {
			return AbstractBakedModel.this.hasDepth();
		}

		@Override
		public boolean isSideLit() {
			return AbstractBakedModel.this.isSideLit();
		}

		@Override
		public boolean isBuiltin() {
			return AbstractBakedModel.this.isBuiltin();
		}

		@Override
		public Sprite getParticleSprite() {
			return AbstractBakedModel.this.getParticleSprite();
		}

		@Override
		public ModelTransformation getTransformation() {
			return AbstractBakedModel.this.itemTransformation;
		}

		@Override
		public ModelOverrideList getOverrides() {
			return AbstractBakedModel.this.getOverrides();
		}
	}

	public final class Baked extends Meshless {
		private final Mesh mesh;

		public Baked(Mesh mesh) {
			this.mesh = mesh;
		}

		@Override
		public void emitBlockQuads(BlockRenderView blockView,
				BlockState state,
				BlockPos pos,
				Supplier<Random> randomSupplier,
				RenderContext context) {
			AbstractBakedModel.this.emitBlockQuads(this.mesh, blockView, state, pos, randomSupplier, context);
		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			AbstractBakedModel.this.emitItemQuads(this.mesh, stack, randomSupplier, context);
		}
	}
}