package net.devtech.sandbox.v0.api;

import org.objectweb.asm.Type;

public interface Sandbox {
	void allow(String internalName);

	default void allow(Class<?> cls) {
		this.allow(Type.getInternalName(cls));
	}

	Class<?> defineValidatedClass(String qualifiedName, byte[] buf, int off, int len);
}
