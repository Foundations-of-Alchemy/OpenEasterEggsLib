package net.devtech.oeel.v0.api.access;

import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;

import net.minecraft.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public interface ItemHasher {
	/**
	 * @return true if the item was hashed
	 */
	boolean hash(ItemKey stack, Hasher hasher);
}
