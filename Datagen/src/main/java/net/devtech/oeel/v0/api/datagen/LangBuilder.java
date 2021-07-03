package net.devtech.oeel.v0.api.datagen;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import net.devtech.oeel.v0.api.util.BiHasher;
import net.devtech.oeel.v0.api.util.OEELEncrypting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record LangBuilder(Map<String, String> lang) {
	public LangBuilder() {
		this(new HashMap<>());
	}

	// todo substitution support

	public LangBuilder addRaw(String translationKey, String translation) {
		this.lang.put(translationKey, translation);
		return this;
	}

	public LangBuilder item(Item input, String entry) {
		return this.item(new ItemStack(input), entry);
	}

	public LangBuilder item(ItemStack input, String entry) {
		BiHasher hasher = BiHasher.createDefault(false);
		hasher.putItem(input);
		String encrypted = OEELEncrypting.encodeBase16(OEELEncrypting.encrypt(entry.getBytes(StandardCharsets.UTF_8), hasher.hashB()));
		this.lang.put("lang.hash." + hasher.hashA(), encrypted);
		return this;
	}

	public JsonObject build() {
		JsonObject object = new JsonObject();
		this.lang.forEach(object::addProperty);
		return object;
	}
}
