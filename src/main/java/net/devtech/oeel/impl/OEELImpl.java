package net.devtech.oeel.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.astrarre.util.v0.api.Id;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.util.BlockData;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("UnstableApiUsage")
public class OEELImpl {
	public static final String MODID = "oeel";
	public static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
	public static final char[] HEX_ARRAY_C = "0123456789abcdef".toCharArray();
	public static final String ALGORITHM = "AES";
	public static final Gson GSON = new Gson();

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

	public static InputStream decryptStream(byte[] keyBytes, InputStream in) throws GeneralSecurityException {
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key);
		return new CipherInputStream(in, c);
	}

	public static OutputStream encryptStream(byte[] keyBytes, OutputStream stream) {
		try {
			Key key = new SecretKeySpec(keyBytes, ALGORITHM);
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			return new CipherOutputStream(stream, c);
		} catch(GeneralSecurityException t) {
			throw Validate.rethrow(t);
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
