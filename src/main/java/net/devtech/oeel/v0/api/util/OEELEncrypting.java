package net.devtech.oeel.v0.api.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;

public class OEELEncrypting {
	/**
	 * {@link Hasher#putLong(long)} to create a second hash for the encryption key.
	 */
	public static final long MAGIC = 0xDEAD_BEEF_CAFE_BABEL;

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
			hexChars[j * 2] = OEELInternal.HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = OEELInternal.HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	public static byte[] decrypt(HashCode key, byte[] data) throws GeneralSecurityException, IOException {
		byte[] keyBytes = new byte[16];
		key.writeBytesTo(keyBytes, 0, keyBytes.length);
		return OEELInternal.crypt(keyBytes, data, Cipher.DECRYPT_MODE);
	}

	public static byte[] encrypt(byte[] data, HashCode key) {
		try {
			byte[] keyBytes = new byte[16];
			key.writeBytesTo(keyBytes, 0, keyBytes.length);
			return OEELInternal.crypt(keyBytes, data, Cipher.ENCRYPT_MODE);
		} catch(GeneralSecurityException e) {
			throw Validate.rethrow(e);
		}
	}
}
