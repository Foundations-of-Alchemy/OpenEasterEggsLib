package net.devtech.oeel.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Id;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.OEELHashing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class OEELInternal {
	public static final String MODID = "oeel";
	public static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
	public static final char[] HEX_ARRAY_C = "0123456789abcdef".toCharArray();

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

	public static <T extends Recipe<?>> RecipeSerializer<T> bridgeSerializer(Function<Identifier, T> create) {
		return new RecipeSerializer<T>() {
			@Override
			public T read(Identifier id, JsonObject json) {
				return create.apply(id);
			}

			@Override
			public T read(Identifier id, PacketByteBuf buf) {
				return create.apply(id);
			}

			@Override
			public void write(PacketByteBuf buf, T recipe) {

			}
		};
	}
}
