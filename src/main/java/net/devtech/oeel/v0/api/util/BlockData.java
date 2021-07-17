package net.devtech.oeel.v0.api.util;

import java.util.function.Predicate;

import io.github.astrarre.util.v0.api.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public class BlockData {
	protected final BlockView world;
	protected final BlockPos pos;
	protected WorldChunk chunk;
	protected BlockState state;
	protected BlockEntity entity;

	public BlockData(@NotNull BlockEntity entity) {
		this(Validate.notNull(entity.getWorld(), "world == null!"), null, entity.getPos(), entity.getCachedState(), entity);
	}

	public BlockData(@NotNull BlockView world,
			@Nullable WorldChunk chunk,
			@NotNull BlockPos pos,
			@Nullable BlockState state,
			@Nullable BlockEntity entity) {
		this.world = Validate.notNull(world, "world == null!");
		this.chunk = chunk;
		this.pos = Validate.notNull(pos, "pos == null!");
		this.state = state;
		this.entity = entity;
	}

	public BlockData(@NotNull BlockView world, @NotNull BlockPos pos) {
		this(world, null, pos, null, null);
	}

	public BlockData(@NotNull BlockView world, @NotNull BlockPos pos, @Nullable BlockState state) {
		this(world, null, pos, state, null);
	}

	public BlockData(@NotNull WorldChunk chunk, @NotNull BlockPos pos) {
		this(chunk.getWorld(), chunk, pos, null, null);
	}

	public BlockData(@NotNull WorldChunk chunk, @NotNull BlockPos pos, @Nullable BlockState state) {
		this(chunk.getWorld(), chunk, pos, state, null);
	}

	public BlockData(@Nullable WorldChunk chunk, @NotNull BlockEntity entity) {
		this(Validate.notNull(entity.getWorld(), "world == null!"), chunk, entity.getPos(), entity.getCachedState(), entity);
	}

	public BlockState getState() {
		BlockState state = this.state;
		if(this.state == null) {
			BlockEntity entity = this.getEntity();
			if(entity == null) {
				this.state = state = this.getChunk().getBlockState(this.pos);
			} else {
				this.state = state = entity.getCachedState();
			}
		}
		return state;
	}

	public WorldChunk getChunk() {
		WorldChunk chunk = this.chunk;
		if(chunk == null) {
			BlockView view = this.world;
			if(view instanceof World w) {
				this.chunk = chunk = w.getWorldChunk(this.pos);
			} else if(view instanceof WorldView v) {
				Chunk c = v.getChunk(this.pos);
				if(c instanceof WorldChunk wc) {
					this.chunk = chunk = wc;
				}
			}
		}
		return chunk;
	}

	public BlockEntity getEntity() {
		BlockEntity entity = this.entity;
		if(entity == null) {
			BlockState state = this.state;
			if(state == null || state.hasBlockEntity()) {
				entity = this.getChunk().getBlockEntity(this.pos);
			}

			if(entity != null && state == null) {
				this.state = entity.getCachedState();
			}
		}
		return entity;
	}

	public BlockView getWorld() {
		return this.world;
	}

	public BlockPos getPos() {
		return this.pos;
	}
}
