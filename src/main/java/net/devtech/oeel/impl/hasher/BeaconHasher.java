package net.devtech.oeel.impl.hasher;

import java.nio.charset.StandardCharsets;

import net.devtech.oeel.impl.mixin.BeaconBlockEntityAccess;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.hash.Hasher;

public record BeaconHasher(String[] hashLevels) implements HashFunction<BlockData> {
	@Override
	public void hash(Hasher hasher, BlockData val) {
		if(val.getEntity() instanceof BeaconBlockEntityAccess b) {
			hasher.putString(this.hashLevels[b.getLevel()], StandardCharsets.US_ASCII);
		}
	}
}
