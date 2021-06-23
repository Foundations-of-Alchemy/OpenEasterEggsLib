package net.devtech.oeel.v0.api.util;

import java.util.function.Predicate;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.resource.ItemHashSubstitutionManager;
import net.devtech.oeel.v0.api.OEELHashing;
import net.devtech.oeel.v0.api.access.ItemHashSubstitution;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public final class ObfuscatedIngredient implements Predicate<ItemKey> {
	private final Identifier substConfig;
	private final ItemHashSubstitution hasher;
	private final HashCode hash;

	public ObfuscatedIngredient(HashCode code, @Nullable Identifier substConfig) {
		this.substConfig = substConfig;
		this.hash = code;
		this.hasher = Validate.transform(substConfig, ItemHashSubstitutionManager::forId);
	}

	@Override
	public boolean test(ItemKey key) {
		HashCode code = OEELHashing.hash(key.createItemStack(1));
		return code.equals(this.hash);
	}

	public static ObfuscatedIngredient read(JsonElement json) {
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

		return new ObfuscatedIngredient(HashCode.fromString(inputHash), substConfig);
	}

	public JsonElement write() {
		JsonPrimitive primitive = new JsonPrimitive(this.hash.toString());
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
