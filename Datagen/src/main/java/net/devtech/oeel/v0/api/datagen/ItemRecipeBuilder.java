package net.devtech.oeel.v0.api.datagen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.v0.api.OEELEncrypting;
import net.devtech.oeel.v0.api.OEELHashing;
import net.devtech.oeel.v0.api.OEELSerializing;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.recipes.ObfuscatedItemRecipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * works for
 * @see BaseObfuscatedRecipe
 * @see ObfuscatedItemRecipe
 */
public class ItemRecipeBuilder {
	private static final Identifier ID = new Identifier("oeel", "empty");
	private final List<String> inputs = new ArrayList<>();
	private final byte[] output;
	private Identifier subst;

	public static ItemRecipeBuilder itemOutput(ItemStack output) {
		return new ItemRecipeBuilder(OEELSerializing.serializeItem(output));
	}

	public static ItemRecipeBuilder itemOutput(Item output) {
		return itemOutput(new ItemStack(output));
	}

	public ItemRecipeBuilder(byte[] output) {
		this.output = output;
	}

	/**
	 * used for substitution
	 */
	public ItemRecipeBuilder direct(String hash) {
		this.inputs.add(hash);
		return this;
	}

	/**
	 * add item input, it should be noted however that this matches the itemstack with no nbt at all
	 */
	public ItemRecipeBuilder item(Item item) {
		return this.stack(new ItemStack(item));
	}

	/**
	 * does not match count
	 */
	public ItemRecipeBuilder stack(ItemStack stack) {
		this.inputs.add(OEELHashing.hash(stack).toString());
		return this;
	}

	public ItemRecipeBuilder substCfg(String modid, String path) {
		this.subst = new Identifier(modid, path);
		return this;
	}

	public ItemRecipeBuilder substCfg(Identifier id) {
		this.subst = id;
		return this;
	}

	/**
	 * used for when the order in which the items are added is the order in which they occur in the inventory (eg. stonecutting, smithing)
	 */
	public JsonObject ordered(Identifier id) {
		Hasher validation = OEELHashing.FUNCTION.newHasher(), encryption = OEELHashing.FUNCTION.newHasher();
		encryption.putLong(OEELEncrypting.MAGIC);
		this.hashId(id, validation);
		this.hashId(id, encryption);
		for(String input : this.inputs) {
			validation.putString(input, StandardCharsets.US_ASCII);
			encryption.putString(input, StandardCharsets.US_ASCII);
		}

		return this.build(validation.hash(), encryption.hash());
	}

	/**
	 * helper to build shaped recipe
	 */
	public JsonObject shaped(int width, int height) {
		Hasher validation = OEELHashing.FUNCTION.newHasher(), encryption = OEELHashing.FUNCTION.newHasher();
		encryption.putLong(OEELEncrypting.MAGIC);

		this.hashId(ObfuscatedCraftingRecipeBridge.SHAPED, validation);
		this.hashId(ObfuscatedCraftingRecipeBridge.SHAPED, encryption);
		validation.putInt(width);
		encryption.putInt(width);
		validation.putInt(height);
		encryption.putInt(height);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				String hash = inputs.get(x + y * width);
				validation.putString(hash, StandardCharsets.US_ASCII);
				encryption.putString(hash, StandardCharsets.US_ASCII);
			}
		}

		return this.build(validation.hash(), encryption.hash());
	}

	public JsonObject shapeless() {
		Hasher validation = OEELHashing.FUNCTION.newHasher(), encryption = OEELHashing.FUNCTION.newHasher();
		encryption.putLong(OEELEncrypting.MAGIC);

		this.hashId(ObfuscatedCraftingRecipeBridge.SHAPELESS, validation);
		this.hashId(ObfuscatedCraftingRecipeBridge.SHAPELESS, encryption);

		List<String> sorted = new ArrayList<>(this.inputs);
		sorted.sort(Comparator.naturalOrder());

		for(String s : sorted) {
			validation.putString(s, StandardCharsets.US_ASCII);
			encryption.putString(s, StandardCharsets.US_ASCII);
		}

		return this.build(validation.hash(), encryption.hash());
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
	
	protected void hashId(Identifier id, Hasher hasher) {
		hasher.putString(id.getNamespace(), StandardCharsets.US_ASCII);
		hasher.putString(id.getPath(), StandardCharsets.US_ASCII);
	}
}
