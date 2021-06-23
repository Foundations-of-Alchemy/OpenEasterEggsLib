package net.devtech.oeel.v0.api.recipes;

import java.lang.reflect.Type;

import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.resource.ItemHashSubstitutionManager;
import net.devtech.oeel.v0.api.access.ItemHashSubstitution;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * A base class for any recipe that uses items.
 * Keep in mind that the input and output can be any combination of anything, it doesn't even technically have to have items.
 * This only exists to provide the {@link ItemHashSubstitution} value.
 * So for example, this exact class, without subclassing could and should be used for smelting when the time is configurable.
 */
public class ObfuscatedItemRecipe extends BaseObfuscatedRecipe {
	public static final Serializer<ObfuscatedItemRecipe> SERIALIZER = new Serializer<>() {
		@Override
		protected ObfuscatedItemRecipe deserialize(JsonObject object, HashCode inputHash, byte[] encryptedOutput, Identifier substId) {
			return new ObfuscatedItemRecipe(inputHash, encryptedOutput, substId);
		}
	};

	/**
	 * the id of the hash substitution function
	 */
	@Nullable
	public final Identifier substitution;

	@Nullable
	public final ItemHashSubstitution subst;

	public ObfuscatedItemRecipe(HashCode hash, byte[] output, Identifier substitution) {
		super(hash, output);
		this.substitution = substitution;
		this.subst = ItemHashSubstitutionManager.forId(substitution);
	}

	public static abstract class Serializer<T extends ObfuscatedItemRecipe> extends BaseObfuscatedRecipe.Serializer<T> {

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject object = super.serialize(src, typeOfSrc, context).getAsJsonObject();
			if(src.substitution != null) {
				object.addProperty("subst", src.substitution.toString());
			}
			return object;
		}

		@Override
		protected T deserialize(JsonObject object, HashCode inputHash, byte[] encryptedOutput) {
			Identifier substId = Validate.transform(Validate.transform(object.getAsJsonPrimitive("subst"), JsonPrimitive::getAsString), Identifier::new);
			return this.deserialize(object, inputHash, encryptedOutput, substId);
		}

		protected abstract T deserialize(JsonObject object, HashCode inputHash, byte[] encryptedOutput, Identifier substId);
	}
}
