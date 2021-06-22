package net.devtech.oeel.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import io.github.astrarre.util.v0.api.Id;
import io.github.astrarre.util.v0.api.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtShort;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("UnstableApiUsage")
public class OEELInternal {
	public static final HashFunction FUNCTION = Hashing.sha256();
	public static final String MODID = "oeel";
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
	/**
	 * if true, unrecognized NbtElement subclasses can be hashed via a default implementation
	 */
	public static boolean COMPAT_MODE = Boolean.getBoolean("compat_mode");

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static Id id2(String path) {
		return Id.create(MODID, path);
	}

	public static HashCode hashWithCount(ItemStack key, HashFunction function) {
		Hasher hasher = function.newHasher();
		OEELInternal.hash(hasher, Registry.ITEM, key.getItem());
		OEELInternal.hash(hasher, key.getTag());
		hasher.putInt(key.getCount());
		return hasher.hash();
	}

	public static HashCode hash(ItemStack key, HashFunction function) {
		Hasher hasher = function.newHasher();
		OEELInternal.hash(hasher, Registry.ITEM, key.getItem());
		OEELInternal.hash(hasher, key.getTag());
		return hasher.hash();
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

	/**
	 * from https://stackoverflow.com/a/20980505/9773993
	 */
	public static byte[] decodeBase16(CharSequence cs) {
		final int numCh = cs.length();
		if((numCh & 1) != 0) {
			throw new IllegalArgumentException("cs must have an even length");
		}
		byte[] array = new byte[numCh >> 1];
		for(int p = 0; p < numCh; p += 2) {
			int hi = Character.digit(cs.charAt(p), 16), lo = Character.digit(cs.charAt(p + 1), 16);
			if((hi | lo) < 0) {
				throw new IllegalArgumentException(cs + " contains non-hex characters");
			}
			array[p >> 1] = (byte) (hi << 4 | lo);
		}
		return array;
	}

	public static String encodeBase16(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for(int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}


	public static final String ALGORITHM = "AES";

	public static ItemStack decryptItem(HashCode key, byte[] data) throws GeneralSecurityException, IOException {
		byte[] keyBytes = new byte[16];
		key.writeBytesTo(keyBytes, 0, keyBytes.length);
		byte[] itemBytes = crypt(keyBytes, data, Cipher.DECRYPT_MODE);
		ByteArrayInputStream bis = new ByteArrayInputStream(itemBytes);
		DataInputStream dis = new DataInputStream(bis);
		return ItemStack.fromNbt(NbtIo.read(dis));
	}

	public static byte[] encryptItem(ItemStack stack, HashCode key) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			NbtIo.write(stack.writeNbt(new NbtCompound()), dos);
			dos.flush();
			byte[] itemBytes = bos.toByteArray();
			byte[] keyBytes = new byte[16];
			key.writeBytesTo(keyBytes, 0, keyBytes.length);
			return crypt(keyBytes, itemBytes, Cipher.ENCRYPT_MODE);
		} catch(Throwable t) {
			throw Validate.rethrow(t);
		}
	}

	public static byte[] crypt(byte[] keyBytes, byte[] inputBytes, int mode) throws GeneralSecurityException {
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(mode, key);
		return c.doFinal(inputBytes);
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
