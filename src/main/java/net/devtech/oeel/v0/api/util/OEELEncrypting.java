package net.devtech.oeel.v0.api.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELImpl;

public class OEELEncrypting {
	/**
	 * from https://stackoverflow.com/a/20980505/9773993
	 */
	public static byte[] decodeBase16(CharSequence cs, int off, int end) {
		int len = end - off;
		if((len & 1) != 0) {
			throw new IllegalArgumentException("cs must have an even length");
		}
		byte[] array = new byte[len >> 1];
		for(int p = off; p < end; p += 2) {
			int hi = Character.digit(cs.charAt(p), 16), lo = Character.digit(cs.charAt(p + 1), 16);
			if((hi | lo) < 0) {
				throw new IllegalArgumentException(cs.subSequence(off, end) + " contains non-hex characters");
			}
			array[(p - off) >> 1] = (byte) (hi << 4 | lo);
		}
		return array;
	}

	public static String encodeBase16(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for(int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = OEELImpl.HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = OEELImpl.HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	public static byte[] decrypt(byte[] key, byte[] data) {
		try {
			return OEELImpl.crypt(key, data, Cipher.DECRYPT_MODE);
		} catch(GeneralSecurityException e) {
			throw Validate.rethrow(e);
		}
	}

	public static byte[] encrypt(byte[] key, byte[] data) {
		try {
			return OEELImpl.crypt(key, data, Cipher.ENCRYPT_MODE);
		} catch(GeneralSecurityException e) {
			throw Validate.rethrow(e);
		}
	}

	public static DataInputStream decryptStream(byte[] key, InputStream data) {
		try {
			return new DataInputStream(OEELImpl.decryptStream(key, data));
		} catch(GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
