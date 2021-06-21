package net.devtech.oeel.v0.api.access;

import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.func.ArrayFunc;
import io.github.astrarre.util.v0.api.func.IterFunc;

import net.minecraft.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public interface ItemHasher {
	IterFunc<ItemHasher> ARRAY_FUNC = array -> (stack, hasher) -> {
		for(ItemHasher h : array) {
			h.hash(stack, hasher);
		}
	};

	void hash(ItemKey stack, Hasher hasher);
}
