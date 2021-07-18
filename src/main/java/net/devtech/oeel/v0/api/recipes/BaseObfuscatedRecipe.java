package net.devtech.oeel.v0.api.recipes;

import java.lang.reflect.Type;

import com.google.common.hash.HashCode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.resource.HashFunctionManager;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * A base obfuscated recipe
 */
public class BaseObfuscatedRecipe {
	public static final Serializer<BaseObfuscatedRecipe> SERIALIZER = new Serializer<>() {
		@Override
		protected BaseObfuscatedRecipe deserialize(JsonObject object,
				HashCode inputHash,
				byte[] encryptedOutput,
				Identifier itemHashFunction,
				Identifier entityHashFunction,
				Identifier blockHashFunction) {
			return new BaseObfuscatedRecipe(inputHash, encryptedOutput, itemHashFunction, entityHashFunction, blockHashFunction);
		}
	};

	public final HashCode inputHash;
	public final byte[] encryptedOutput;
	@Nullable
	public final Identifier itemHashFunction, entityHashFunction, blockHashFunction;
	protected HashFunction<ItemKey> item;
	protected HashFunction<Entity> entity;
	protected HashFunction<BlockData> block;

	public BaseObfuscatedRecipe(HashCode hash,
			byte[] output,
			@Nullable Identifier itemHashFunction,
			@Nullable Identifier entityHashFunction,
			@Nullable Identifier blockHashFunction) {
		this.inputHash = hash;
		this.encryptedOutput = output;
		this.itemHashFunction = itemHashFunction;
		this.entityHashFunction = entityHashFunction;
		this.blockHashFunction = blockHashFunction;
	}

	public HashFunction<ItemKey> getItemHashFunction() {
		HashFunction<ItemKey> item = this.item;
		if(item == null) {
			Identifier id = this.itemHashFunction;
			if(id == null) return null;
			this.item = item = HashFunctionManager.item(id);
		}
		return item;
	}

	public HashFunction<Entity> getEntityHashFunction() {
		HashFunction<Entity> entity = this.entity;
		if(entity == null) {
			Identifier id = this.entityHashFunction;
			if(id == null) return null;
			this.entity = entity = HashFunctionManager.entity(id);
		}
		return entity;
	}

	public HashFunction<BlockData> getBlockHashFunction() {
		HashFunction<BlockData> block = this.block;
		if(block == null) {
			Identifier id = this.blockHashFunction;
			if(id == null) return null;
			this.block = block = HashFunctionManager.block(id);
		}
		return block;
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
			String itemHashFunc = Validate.transform(object.getAsJsonPrimitive("item"), JsonPrimitive::getAsString);
			String entityHashFunc = Validate.transform(object.getAsJsonPrimitive("entity"), JsonPrimitive::getAsString);
			String blockHashFunc = Validate.transform(object.getAsJsonPrimitive("block"), JsonPrimitive::getAsString);
			return this.deserialize(object,
			                        HashCode.fromString(inputHashString),
			                        OEELEncrypting.decodeBase16(encryptedOutputString),
			                        Validate.transform(itemHashFunc, Identifier::new),
			                        Validate.transform(entityHashFunc, Identifier::new),
			                        Validate.transform(blockHashFunc, Identifier::new));
		}

		protected abstract T deserialize(JsonObject object,
				HashCode inputHash,
				byte[] encryptedOutput,
				Identifier itemHashFunction,
				Identifier entityHashFunction,
				Identifier blockHashFunction);
	}
}
