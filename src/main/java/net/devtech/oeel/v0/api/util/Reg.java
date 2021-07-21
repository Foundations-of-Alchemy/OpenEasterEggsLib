package net.devtech.oeel.v0.api.util;

import java.util.HashMap;
import java.util.Map;

import io.github.astrarre.util.v0.api.Validate;

import net.minecraft.util.Identifier;

public class Reg<T> {
	protected final Map<Identifier, T> map = new HashMap<>();

	public void register(Identifier id, T val) {
		Validate.notNull(this.map.put(id, val), "replaced entry " + id + ", try-catch if intended");
	}

	public T get(Identifier id) {
		return this.map.get(id);
	}
}
