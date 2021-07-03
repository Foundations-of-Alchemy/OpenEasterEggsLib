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
import net.devtech.oeel.impl.resource.HashSubstitutionManager;
import net.devtech.oeel.v0.api.access.HashSubstitution;
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
				Identifier itemHashSubstitution,
				Identifier entityHashSubstitution,
				Identifier blockHashSubstitution) {
			return new BaseObfuscatedRecipe(inputHash, encryptedOutput, itemHashSubstitution, entityHashSubstitution, blockHashSubstitution);
		}
	};

	public final HashCode inputHash;
	public final byte[] encryptedOutput;
	@Nullable
	public final Identifier itemHashSubstitution, entityHashSubstitution, blockHashSubstitution;
	protected HashSubstitution<ItemKey> item;
	protected HashSubstitution<Entity> entity;
	protected HashSubstitution<BlockData> block;

	public BaseObfuscatedRecipe(HashCode hash,
			byte[] output,
			@Nullable Identifier itemHashSubstitution,
			@Nullable Identifier entityHashSubstitution,
			@Nullable Identifier blockHashSubstitution) {
		this.inputHash = hash;
		this.encryptedOutput = output;
		this.itemHashSubstitution = itemHashSubstitution;
		this.entityHashSubstitution = entityHashSubstitution;
		this.blockHashSubstitution = blockHashSubstitution;
	}

	public HashSubstitution<ItemKey> getItemHashSubstitution() {
		HashSubstitution<ItemKey> item = this.item;
		if(item == null) {
			Identifier id = this.itemHashSubstitution;
			if(id == null) return null;
			this.item = item = HashSubstitutionManager.item(id);
		}
		return item;
	}

	public HashSubstitution<Entity> getEntityHashSubstitution() {
		HashSubstitution<Entity> entity = this.entity;
		if(entity == null) {
			Identifier id = this.entityHashSubstitution;
			if(id == null) return null;
			this.entity = entity = HashSubstitutionManager.entity(id);
		}
		return entity;
	}

	public HashSubstitution<BlockData> getBlockHashSubstitution() {
		HashSubstitution<BlockData> block = this.block;
		if(block == null) {
			Identifier id = this.blockHashSubstitution;
			if(id == null) return null;
			this.block = block = HashSubstitutionManager.block(id);
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
			String itemHashSubst = Validate.transform(object.getAsJsonPrimitive("item_subst"), JsonPrimitive::getAsString);
			String entityHashSubst = Validate.transform(object.getAsJsonPrimitive("entity_subst"), JsonPrimitive::getAsString);
			String blockHashSubst = Validate.transform(object.getAsJsonPrimitive("block_subst"), JsonPrimitive::getAsString);
			return this.deserialize(object,
			                        HashCode.fromString(inputHashString),
			                        OEELEncrypting.decodeBase16(encryptedOutputString),
			                        Validate.transform(itemHashSubst, Identifier::new),
			                        Validate.transform(entityHashSubst, Identifier::new),
			                        Validate.transform(blockHashSubst, Identifier::new));
		}

		protected abstract T deserialize(JsonObject object,
				HashCode inputHash,
				byte[] encryptedOutput,
				Identifier itemHashSubstitution,
				Identifier entityHashSubstitution,
				Identifier blockHashSubstitution);
	}
}
