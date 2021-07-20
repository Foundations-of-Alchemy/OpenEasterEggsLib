package net.devtech.oeel.impl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Id;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class OEELInternal {
	public static final String MODID = "oeel";
	public static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
	public static final char[] HEX_ARRAY_C = "0123456789abcdef".toCharArray();
	public static final String ALGORITHM = "AES";
	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static Id id2(String path) {
		return Id.create(MODID, path);
	}

	public static byte[] crypt(byte[] keyBytes, byte[] inputBytes, int mode) throws GeneralSecurityException {
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(mode, key);
		return c.doFinal(inputBytes);
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
