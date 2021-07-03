package net.devtech.oeel.impl.hasher;

import net.devtech.oeel.impl.mixin.BeaconBlockEntityAccess;
import net.devtech.oeel.v0.api.access.HashSubstitution;
import net.devtech.oeel.v0.api.util.BlockData;

public record BeaconHasher(String[] hashLevels) implements HashSubstitution<BlockData> {
	@Override
	public String substitute(String incoming, BlockData val) {
		if(val.getEntity() instanceof BeaconBlockEntityAccess b) {
			return this.hashLevels[b.getLevel()];
		}
		return incoming;
	}
}
