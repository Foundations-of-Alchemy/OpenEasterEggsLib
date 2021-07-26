package net.devtech.sandbox.impl;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * todo method replacing / handlers
 */
public class SandboxingClassVisitor extends ClassVisitor implements Opcodes {
	final Set<String> allowedClasses;
	String owner;

	public SandboxingClassVisitor(ClassVisitor delegate, Set<String> classes) {
		super(Opcodes.ASM9, delegate);
		this.allowedClasses = classes;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if(version > Opcodes.V16) {
			throw new UnsupportedOperationException("higher than supported class version!");
		}
		this.owner = name;

		if(!this.allowedClasses.contains(superName)) {
			throw new SandboxViolationException("super class " + superName + " is not allowed!");
		}

		for(String iface : interfaces) {
			if(!this.allowedClasses.contains(iface)) {
				throw new SandboxViolationException("interface class " + iface + " is not allowed!");
			}
		}

		// signature is irrelavent

		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if(this.validateType(descriptor)) {
			throw new SandboxViolationException("field class " + descriptor + " of \"" + name + "\" is not allowed!");
		}

		return super.visitField(access, name, descriptor, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		Type method = Type.getMethodType(descriptor);
		Type[] args = method.getArgumentTypes();
		for(Type arg : args) {
			if(this.validateType(arg)) {
				throw new SandboxViolationException("method arg " + arg + " of \"" + name + "\" is not allowed!");
			}
		}

		Type ret = method.getReturnType();
		if(this.validateType(ret)) {
			throw new SandboxViolationException("return type " + ret + " of \"" + name + "\" is not allowed!");
		}

		return new SandboxingMethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions), name);
	}

	public boolean validateType(Type type) {
		if(type.getSort() == Type.METHOD) {
			Type[] args = type.getArgumentTypes();
			for(Type arg : args) {
				if(this.validateType(arg)) {
					return true;
				}
			}

			return this.validateType(type.getReturnType());
		}

		while(type.getSort() == Type.ARRAY) {
			type = type.getElementType();
		}

		if(type.getSort() == Type.OBJECT) {
			return !this.allowedClasses.contains(type.getInternalName());
		} else {
			return false;
		}
	}

	public boolean validateType(String descriptor) {
		return this.validateType(Type.getType(descriptor));
	}

	public class SandboxingMethodVisitor extends MethodVisitor {
		final String name;
		int ln = -1;

		public SandboxingMethodVisitor(int api, MethodVisitor methodVisitor, String name) {
			super(api, methodVisitor);
			this.name = name;
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			if(SandboxingClassVisitor.this.validateType(type)) {
				String op;
				switch(opcode) {
					case CHECKCAST -> op = "cast";
					case INSTANCEOF -> op = "instanceof";
					case NEW -> op = "instantiation";
					case ANEWARRAY -> op = "array instantiation";
					default -> op = "idk";
				}
				throw new SandboxViolationException(op + " is of invalid type " + type);
			}
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if(!SandboxingClassVisitor.this.allowedClasses.contains(owner)) {
				throw this.create("method owner");
			} else if(SandboxingClassVisitor.this.validateType(Type.getMethodType(descriptor))) {
				throw this.create("method desc");
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
			if(this.validateHandle(bootstrapMethodHandle)) {
				throw this.create("indy handle");
			} else if(SandboxingClassVisitor.this.validateType(Type.getMethodType(descriptor))) {
				throw this.create("indy desc");
			} else for(Object argument : bootstrapMethodArguments) {
				this.validateConstant(argument);
			}

			super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
			if(!SandboxingClassVisitor.this.allowedClasses.contains(owner)) {
				throw this.create("field owner " + owner + "." + name);
			} else if(SandboxingClassVisitor.this.validateType(descriptor)) {
				throw this.create("field desc of " + owner + "." + name);
			}
			super.visitFieldInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
			if(SandboxingClassVisitor.this.validateType(Type.getType(descriptor))) {
				throw this.create("array init " + "[]".repeat(numDimensions));
			}
			super.visitMultiANewArrayInsn(descriptor, numDimensions);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			if(SandboxingClassVisitor.this.validateType(type)) {
				throw this.create("try-catch block");
			}
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public void visitLdcInsn(Object value) {
			this.validateConstant(value);
			super.visitLdcInsn(value);
		}

		@Override
		public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
			if(SandboxingClassVisitor.this.validateType(descriptor)) {
				// this is here for enhanced errors
				throw this.create("local variable \"" + name + "\" of " + descriptor);
			}
			super.visitLocalVariable(name, descriptor, signature, start, end, index);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			this.ln = line;
			super.visitLineNumber(line, start);
		}

		public void validateConstant(Object value) {
			if(value instanceof Handle h) {
				if(this.validateHandle(h)) {
					throw this.create("MethodHandle constant");
				}
			} else if(value instanceof Type t) {
				if(SandboxingClassVisitor.this.validateType(t)) {
					throw this.create("Class constant of " + t);
				}
			} else if(value instanceof ConstantDynamic d) {
				Handle handle = d.getBootstrapMethod();
				if(this.validateHandle(handle)) {
					throw this.create("ConstantDynamic MethodHandle");
				} else {
					for(int i = 0; i < d.getBootstrapMethodArgumentCount(); i++) {
						Object constant = d.getBootstrapMethodArgument(i);
						this.validateConstant(constant);
					}
				}
			}
		}

		SandboxViolationException create(String problem) {
			return new SandboxViolationException(problem + " in " + this.name + " is invalid! line: " + this.ln);
		}

		public boolean validateHandle(Handle handle) {
			return !SandboxingClassVisitor.this.allowedClasses.contains(handle.getOwner()) ||
			       SandboxingClassVisitor.this.validateType(Type.getMethodType(handle.getDesc()));
		}
	}
}
