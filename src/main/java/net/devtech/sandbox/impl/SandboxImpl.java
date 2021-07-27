package net.devtech.sandbox.impl;

import java.util.HashSet;
import java.util.Set;

import net.devtech.sandbox.v0.api.Sandbox;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class SandboxImpl extends ClassLoader implements Sandbox {
	final Set<String> allowed = new HashSet<>();

	@Override
	public void allow(String internalName) {
		this.allowed.add(internalName);
	}

	@Override
	public Class<?> defineValidatedClass(byte[] buf, int off, int len) {
		ClassWriter writer = new ClassWriter(0);
		ClassVisitor visitor = new SandboxingClassVisitor(writer, this.allowed);
		ClassReader reader = new ClassReader(buf, off, len);
		reader.accept(visitor, 0);
		byte[] code = writer.toByteArray();
		String qualifiedName = reader.getClassName().replace('/', '.');
		return this.defineClass(qualifiedName, code, 0, code.length);
	}
}
