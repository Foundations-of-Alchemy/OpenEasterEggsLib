package net.devtech.oeel.v0.api.datagen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.hash.HashCode;
import com.google.gson.JsonObject;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BiHasher;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.OEELHashing;
import net.devtech.oeel.v0.api.util.OEELSerializing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * works for
 * @see BaseObfuscatedRecipe
 */
public class RecipeBuilder {
	private static final Identifier ID = new Identifier("oeel", "empty");
	private final List<String> inputs = new ArrayList<>();
	private final byte[] output;
	private Identifier subst;

	public static RecipeBuilder itemOutput(ItemStack output) {
		return new RecipeBuilder(OEELSerializing.serializeItem(output));
	}

	public static RecipeBuilder itemOutput(Item output) {
		return itemOutput(new ItemStack(output));
	}

	public RecipeBuilder(byte[] output) {
		this.output = output;
	}

	/**
	 * todo substitution json builder and support
	 * used in case of substitution
	 */
	public RecipeBuilder direct(String hash) {
		this.inputs.add(hash);
		return this;
	}

	/**
	 * add item input, it should be noted however that this matches the itemstack with no nbt at all
	 */
	public RecipeBuilder item(Item item) {
		return this.stack(new ItemStack(item));
	}

	/**
	 * does not match count
	 */
	public RecipeBuilder stack(ItemStack stack) {
		this.inputs.add(OEELHashing.hash(stack).toString());
		return this;
	}

	public RecipeBuilder substCfg(String modid, String path) {
		this.subst = new Identifier(modid, path);
		return this;
	}

	public RecipeBuilder substCfg(Identifier id) {
		this.subst = id;
		return this;
	}

	/**
	 * used for when the order in which the items are added is the order in which they occur in the inventory (eg. stonecutting, smithing)
	 */
	public JsonObject ordered(Identifier id) {
		BiHasher hasher = BiHasher.createDefault(false);
		hasher.putIdentifier(id);

		for(String input : this.inputs) {
			hasher.putString(input, StandardCharsets.US_ASCII);
		}

		return this.build(hasher.hashA(), hasher.hashB());
	}

	/**
	 * helper to build shaped recipe
	 */
	public JsonObject shaped(int width, int height) {
		BiHasher hasher = BiHasher.createDefault(false);
		hasher.putIdentifier(ObfuscatedCraftingRecipeBridge.SHAPED);
		hasher.putInt(width);
		hasher.putInt(height);

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				String hash = this.inputs.get(x + y * width);
				hasher.putString(hash, StandardCharsets.US_ASCII);
			}
		}

		return this.build(hasher.hashA(), hasher.hashB());
	}

	public JsonObject shapeless() {
		BiHasher hasher = BiHasher.createDefault(false);
		hasher.putIdentifier(ObfuscatedCraftingRecipeBridge.SHAPELESS);

		List<String> sorted = new ArrayList<>(this.inputs);
		sorted.sort(Comparator.naturalOrder());

		for(String s : sorted) {
			hasher.putString(s, StandardCharsets.US_ASCII);
		}

		return this.build(hasher.hashA(), hasher.hashB());
	}

	protected JsonObject build(HashCode validation, HashCode encryption) {
		JsonObject fin = new JsonObject();
		fin.addProperty("input", validation.toString());
		fin.addProperty("output", OEELEncrypting.encodeBase16(OEELEncrypting.encrypt(this.output, encryption)));
		if(this.subst != null) {
			fin.addProperty("subst", this.subst.toString());
		}
		return fin;
	}
}
