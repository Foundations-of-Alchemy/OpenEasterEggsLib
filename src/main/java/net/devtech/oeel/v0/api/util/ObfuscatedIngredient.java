package net.devtech.oeel.v0.api.util;

import java.util.function.Predicate;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.ItemHasher;

@SuppressWarnings("UnstableApiUsage")
public record ObfuscatedIngredient(HashFunction function, ItemHasher hasher, String hash) implements Predicate<ItemKey> {
	@Override
	public boolean test(ItemKey key) {
		Hasher hasher = this.function.newHasher();
		Validate.isTrue(this.hasher.hash(key, hasher), key + " was not hashed!");
		HashCode code = hasher.hash();
		return code.toString().equals(this.hash);
	}
}
