package net.devtech.oeel.v0.api.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import org.jetbrains.annotations.NotNull;

public class OEELEncrypting {
	/**
	 * from https://stackoverflow.com/a/20980505/9773993
	 */
	public static byte[] decodeBase16(CharSequence cs, int off, int end) {
		if((end & 1) != 0) {
			throw new IllegalArgumentException("cs must have an even length");
		}
		byte[] array = new byte[end >> 1];
		for(int p = 0; p < end; p += 2) {
			int hi = Character.digit(cs.charAt(p + off), 16), lo = Character.digit(cs.charAt(p + 1), 16);
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

	public static byte[] decrypt(byte[] key, byte[] data) {
		try {
			return OEELInternal.crypt(key, data, Cipher.DECRYPT_MODE);
		} catch(GeneralSecurityException e) {
			throw Validate.rethrow(e);
		}
	}

	public static byte[] encrypt(byte[] key, byte[] data) {
		try {
			return OEELInternal.crypt(key, data, Cipher.ENCRYPT_MODE);
		} catch(GeneralSecurityException e) {
			throw Validate.rethrow(e);
		}
	}

	public static DataInputStream decryptStream(byte[] key, byte[] data) {
		try {
			return new DataInputStream(OEELInternal.decryptStream(key, new ByteArrayInputStream(data)));
		} catch(GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
