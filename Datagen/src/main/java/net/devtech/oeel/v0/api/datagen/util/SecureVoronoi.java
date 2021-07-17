package net.devtech.oeel.v0.api.datagen.util;

import java.security.SecureRandom;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public record SecureVoronoi(long seed, int sizeX, int sizeY) {
	public int voronoi(int pixelX, int pixelY) {
		return secureVoronoi(pixelX, pixelY, this.seed, this.sizeX, this.sizeY);
	}

	public static int secureVoronoi(int pixelX, int pixelY, long seed, int sizeX, int sizeY) {
		int cellX = pixelX / sizeX, cellY = pixelY / sizeY;
		int currentMin = Integer.MAX_VALUE, currentValue = 0;
		for(int offX = -1; offX <= 1; offX++) {
			for(int offY = -1; offY <= 1; offY++) {
				VoronoiValue val = secureVoronoiCell(pixelX, pixelY, cellX + offX, cellY + offY, sizeX, sizeY, seed);
				if(val.squaredDistance < currentMin) {
					currentMin = val.squaredDistance;
					currentValue = val.rand;
				}
			}
		}

		return currentValue;
	}

	record VoronoiValue(int squaredDistance, int rand) {}

	protected static VoronoiValue secureVoronoiCell(int x, int y, int cellX, int cellY, int sizeX, int sizeY, long seed) {
		try {
			Hasher hasher = Hashing.sha256().newHasher(16);
			hasher.putInt(cellX);
			hasher.putInt(cellY);
			hasher.putLong(seed);
			byte[] bytes = hasher.hash().asBytes();
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(bytes);
			int cellXPos = (cellX * sizeX) + random.nextInt(sizeX), cellYPos = (cellY * sizeY) + random.nextInt(sizeY);
			int cellXDist = cellXPos - x, cellYDist = cellYPos - y;
			int distance = cellXDist * cellXDist + cellYDist * cellYDist;
			int rand = random.nextInt();
			return new VoronoiValue(distance, rand);
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
