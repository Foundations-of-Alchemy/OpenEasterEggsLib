package net.devtech.oeel.v0.api.recipes;

import java.lang.reflect.Type;

import com.google.common.hash.HashCode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.OEELEncrypting;

/**
 * A base obfuscated recipe
 * @see ObfuscatedItemRecipe
 */
public class BaseObfuscatedRecipe {
	public static final Serializer<BaseObfuscatedRecipe> SERIALIZER = new Serializer<>() {
		@Override
		protected BaseObfuscatedRecipe deserialize(JsonObject object, HashCode inputHash, byte[] encryptedOutput) {
			return new BaseObfuscatedRecipe(inputHash, encryptedOutput);
		}
	};

	public final HashCode inputHash;
	public final byte[] encryptedOutput;

	public BaseObfuscatedRecipe(HashCode hash, byte[] output) {
		this.inputHash = hash;
		this.encryptedOutput = output;
	}

	public static abstract class Serializer<T extends BaseObfuscatedRecipe> implements JsonSerializer<T>, JsonDeserializer<T> {
		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("input", src.inputHash.toString());
			object.addProperty("output", OEELEncrypting.encodeBase16(src.encryptedOutput));
			return object;
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			String inputHashString = Validate.notNull(object.getAsJsonPrimitive("input"), "recipe must have input!").getAsString();
			String encryptedOutputString = Validate.notNull(object.getAsJsonPrimitive("output"), "recipe must have output!").getAsString();
			return this.deserialize(object, HashCode.fromString(inputHashString), OEELEncrypting.decodeBase16(encryptedOutputString));
		}

		protected abstract T deserialize(JsonObject object, HashCode inputHash, byte[] encryptedOutput);
	}
}
