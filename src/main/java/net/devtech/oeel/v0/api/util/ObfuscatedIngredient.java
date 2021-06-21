package net.devtech.oeel.v0.api.util;

import java.util.function.Predicate;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.access.ItemHasher;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public final class ObfuscatedIngredient implements Predicate<ItemKey> {
	private final HashFunction function;
	private final Identifier substConfig;
	private final ItemHasher hasher;
	private final byte[] hash;

	public ObfuscatedIngredient(HashFunction function, @Nullable Identifier substConfig, byte[] hash) {
		this.function = function;
		this.substConfig = substConfig;
		this.hash = hash;
		this.hasher = OEELInternal.from(substConfig);
	}

	@Override
	public boolean test(ItemKey key) {
		Hasher hasher = this.function.newHasher();
		this.hasher.hash(key, hasher);
		HashCode code = hasher.hash();
		return code.equals(HashCode.fromBytes(this.hash));
	}

	public static ObfuscatedIngredient read(Identifier id, JsonElement json) {
		String inputHash;
		Identifier substConfig;
		if(json.isJsonPrimitive()) {
			inputHash = json.getAsString();
			substConfig = null;
		} else if(json instanceof JsonObject o){
			inputHash = Validate.notNull(o.getAsJsonPrimitive("hash"), "no input").getAsString();
			substConfig = Validate.transform(Validate.transform(o.getAsJsonPrimitive("subst"), JsonPrimitive::getAsString), Identifier::new);
		} else {
			throw new IllegalArgumentException("Invalid json type");
		}

		byte[] inputHashBytes = OEELInternal.decodeBase16(inputHash);

		return new ObfuscatedIngredient(OEELInternal.FUNCTION, substConfig, inputHashBytes);
	}

	public JsonElement write() {
		JsonPrimitive primitive = new JsonPrimitive(OEELInternal.encodeBase16(this.hash));
		if(this.substConfig == null) {
			return primitive;
		} else {
			JsonObject object = new JsonObject();
			object.add("hash", primitive);
			object.addProperty("subst", this.substConfig.toString());
			return object;
		}
	}
}
