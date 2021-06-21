package net.devtech.oeel.v0.api.access;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.func.IterFunc;

public interface ItemHashSubstitution {
	IterFunc<ItemHashSubstitution> COMBINE = iter -> (incoming, val) -> {
		for(ItemHashSubstitution substitution : iter) {
			String v = substitution.substitute(incoming, val);
			if(!v.equals(incoming)) {
				return v;
			}
		}
		return incoming;
	};

	String substitute(String incoming, ItemKey val);
}
