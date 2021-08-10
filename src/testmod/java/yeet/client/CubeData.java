package yeet.client;

import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class CubeData {
	public final Multimap<Direction, FaceData> identifiers = ArrayListMultimap.create(6, 1);

	public CubeData() {
	}

	/**
	 * makes north this direction
	 */
	public CubeData rotate(Direction direction) {
		CubeData data = new CubeData();
		data.identifiers.putAll(direction, this.identifiers.get(Direction.NORTH));
		Direction clockwise = getClockwise(direction);
		data.identifiers.putAll(clockwise, this.identifiers.get(Direction.EAST));
		data.identifiers.putAll(clockwise.getOpposite(), this.identifiers.get(Direction.WEST));
		data.identifiers.putAll(direction.getOpposite(), this.identifiers.get(Direction.SOUTH));
		Direction up = getUp(direction);
		data.identifiers.putAll(up, this.identifiers.get(Direction.UP));
		data.identifiers.putAll(up.getOpposite(), this.identifiers.get(Direction.DOWN));
		return data;
	}

	public static CubeData withAll(Identifier identifier) {
		CubeData data = new CubeData();
		FaceData face = new FaceData(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier), false);
		for (int i = 0; i < 6; i++) {
			data.identifiers.put(Direction.byId(i), face);
		}
		return data;
	}

	public static CubeData withAll(SpriteIdentifier identifier) {
		CubeData data = new CubeData();
		FaceData face = new FaceData(identifier, false);
		for (int i = 0; i < 6; i++) {
			data.identifiers.put(Direction.byId(i), face);
		}
		return data;
	}

	public CubeData addBlock(Direction direction, Identifier identifier, boolean isEmissive) {
		return this.add(direction, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier), isEmissive);
	}

	/**
	 * add an overlay texture
	 */
	public CubeData add(Direction direction, SpriteIdentifier identifier, boolean isEmissive) {
		CubeData cubeData = new CubeData();
		cubeData.identifiers.putAll(this.identifiers);
		cubeData.identifiers.put(direction, new FaceData(identifier, isEmissive));
		return cubeData;
	}

	public CubeData withBlock(Direction direction, Identifier identifier, boolean isEmissive) {
		return this.set(direction, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, identifier), isEmissive);
	}

	public CubeData set(Direction direction, SpriteIdentifier identifier, boolean isEmissive) {
		CubeData cubeData = new CubeData();
		cubeData.identifiers.putAll(this.identifiers);
		cubeData.identifiers.removeAll(direction);
		cubeData.identifiers.put(direction, new FaceData(identifier, isEmissive));
		return cubeData;
	}

	public Collection<FaceData> get(Direction direction) {
		return this.identifiers.get(direction);
	}

	public static final class FaceData {
		public final SpriteIdentifier identifier;
		public final boolean isEmissive;

		public FaceData(SpriteIdentifier identifier, boolean emissive) {
			this.identifier = identifier;
			this.isEmissive = emissive;
		}
	}

	public static Direction getClockwise(Direction direction) {
		return switch(direction) {
			case UP, NORTH -> Direction.EAST;
			case DOWN, SOUTH -> Direction.WEST;
			case EAST -> Direction.SOUTH;
			case WEST -> Direction.NORTH;
		};
	}

	public static Direction getUp(Direction direction) {
		return switch(direction) {
			case DOWN -> Direction.NORTH;
			case UP -> Direction.SOUTH;
			default -> Direction.UP;
		};
	}
}