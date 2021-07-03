package net.devtech.oeel.v0.api.access;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.func.IterFunc;

public interface HashSubstitution<T> {
	IterFunc<HashSubstitution<?>> COMBINE = iter -> (incoming, val) -> {
		for(HashSubstitution substitution : iter) {
			String v = substitution.substitute(incoming, val);
			if(!v.equals(incoming)) {
				return v;
			}
		}
		return incoming;
	};

	static  <T> IterFunc<HashSubstitution<T>> combine() {
		return (IterFunc)COMBINE;
	}

	String substitute(String incoming, T val);
}
