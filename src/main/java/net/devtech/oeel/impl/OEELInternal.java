package net.devtech.oeel.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.Hasher;
import io.github.astrarre.util.v0.api.Id;
import net.devtech.oeel.v0.api.OEELHashing;

import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class OEELInternal {
	public static final String MODID = "oeel";
	public static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static Id id2(String path) {
		return Id.create(MODID, path);
	}


	public static byte[] crypt(byte[] keyBytes, byte[] inputBytes, int mode) throws GeneralSecurityException {
		Key key = new SecretKeySpec(keyBytes, OEELHashing.ALGORITHM);
		Cipher c = Cipher.getInstance(OEELHashing.ALGORITHM);
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
