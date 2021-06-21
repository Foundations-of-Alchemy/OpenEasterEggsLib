package net.devtech.oeel.impl;

import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.hash.Hasher;
import io.github.astrarre.util.v0.api.Val;
import io.github.astrarre.util.v0.api.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtShort;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("UnstableApiUsage")
public class OEELInternal {
	/**
	 * if true, unrecognized NbtElement subclasses can be hashed via a default implementation
	 */
	public static boolean COMPAT_MODE = Boolean.getBoolean("compat_mode");

	public static final String MODID = "oeel";

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static <T> void hash(Hasher hasher, Registry<T> registry, T item) {
		hasher.putUnencodedChars(registry.getId(item).toString());
	}

	public static void hash(Hasher hasher, @Nullable NbtElement element) {
		if(element == null) {
			hasher.putUnencodedChars("empty");
		} else if(element instanceof NbtCompound n) {
			if(n.isEmpty()) {
				hasher.putUnencodedChars("empty");
			} else {
				for(String key : (Iterable<String>) () -> n.getKeys().stream().sorted().iterator()) {
					hasher.putUnencodedChars(key);
					hash(hasher, n.get(key));
				}
			}
		} else if(element instanceof NbtByteArray bs) {
			for(int i = 0; i < bs.size(); i++) {
				hasher.putInt(i);
				hasher.putByte(bs.getByteArray()[i]);
			}
		} else if(element instanceof NbtIntArray is) {
			for(int i = 0; i < is.size(); i++) {
				hasher.putInt(i);
				hasher.putInt(is.getIntArray()[i]);
			}
		} else if(element instanceof NbtLongArray ls) {
			for(int i = 0; i < ls.size(); i++) {
				hasher.putInt(i);
				hasher.putLong(ls.getLongArray()[i]);
			}
		} else if(element instanceof AbstractNbtList<?> l) {
			for(int i = 0, size = l.size(); i < size; i++) {
				NbtElement e = l.get(i);
				hasher.putInt(i);
				hash(hasher, e);
			}
		} else if(element instanceof NbtByte b) {
			hasher.putByte(b.byteValue());
		} else if(element instanceof NbtShort s) {
			hasher.putShort(s.shortValue());
		} else if(element instanceof NbtInt i) {
			hasher.putInt(i.intValue());
		} else if(element instanceof NbtFloat f) {
			hasher.putFloat(f.floatValue());
		} else if(element instanceof NbtLong l) {
			hasher.putLong(l.longValue());
		} else if(element instanceof NbtDouble d) {
			hasher.putDouble(d.doubleValue());
		} else if(element instanceof NbtNull) {
			hasher.putUnencodedChars("null");
		} else if(COMPAT_MODE) {
			HashingOutput output = new HashingOutput(hasher);
			try {
				element.write(output);
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		} else {
			throw new UnsupportedOperationException("fail");
		}
	}

	public static class HashingOutput extends DataOutputStream {
		private final Hasher hasher;
		public HashingOutput(Hasher hasher) {
			super(NullOutputStream.NULL);
			this.hasher = hasher;
		}

		@Override
		public synchronized void write(int b) throws IOException {
			this.hasher.putByte((byte) b);
			super.write(b);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			this.hasher.putBytes(b, off, len);
			super.write(b, off, len);
		}
	}
}
