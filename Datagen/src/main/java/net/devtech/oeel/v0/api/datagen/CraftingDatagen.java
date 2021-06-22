package net.devtech.oeel.v0.api.datagen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.shaped.ObfuscatedShapedCraftingRecipe;
import net.devtech.oeel.impl.shaped.ObfuscatedShapelessCraftingRecipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CraftingDatagen {
	private static final Identifier ID = new Identifier("oeel", "empty");
	private final List<String> inputs = new ArrayList<>();
	private final ItemStack output;
	private Identifier subst;

	public static CraftingDatagen builder(ItemStack output) {
		return new CraftingDatagen(output);
	}

	public static CraftingDatagen builder(Item output) {
		return new CraftingDatagen(output);
	}

	public CraftingDatagen(ItemStack output) {
		this.output = output;
	}

	public CraftingDatagen(Item output) {
		this(new ItemStack(output));
	}

	/**
	 * used for substitution
	 */
	public CraftingDatagen addDirect(String hash) {
		this.inputs.add(hash);
		return this;
	}

	/**
	 * add item input, it should be noted however that this matches the itemstack with no nbt at all
	 */
	public CraftingDatagen addItem(Item item) {
		return this.addItemStack(new ItemStack(item));
	}

	/**
	 * does not match count
	 */
	public CraftingDatagen addItemStack(ItemStack stack) {
		this.inputs.add(OEELInternal.hash(stack, OEELInternal.FUNCTION).toString());
		return this;
	}

	public CraftingDatagen hashSubstitutionConfig(String modid, String path) {
		this.subst = new Identifier(modid, path);
		return this;
	}

	public CraftingDatagen hashSubstitutionConfig(Identifier id) {
		this.subst = id;
		return this;
	}

	public JsonObject shaped(int width, int height) {
		Hasher validation = OEELInternal.FUNCTION.newHasher(), encryption = OEELInternal.FUNCTION.newHasher();
		encryption.putUnencodedChars("decrypted output");
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				String s = this.inputs.get(x + y * width);
				validation.putUnencodedChars(s);
				encryption.putUnencodedChars(s);
			}
		}

		ObfuscatedShapedCraftingRecipe shaped = new ObfuscatedShapedCraftingRecipe(ID,
		                                                                           validation.hash(),
		                                                                           this.subst,
		                                                                           OEELInternal.encryptItem(this.output, encryption.hash()),
		                                                                           width,
		                                                                           height,
		                                                                           OEELInternal.FUNCTION);
		JsonObject object = new JsonObject();
		object.addProperty("type", "oeel:obf_shaped");
		ObfuscatedShapedCraftingRecipe.SERIALIZER.write(object, shaped);
		return object;
	}

	public JsonObject shapeless() {
		Hasher validation = OEELInternal.FUNCTION.newHasher(), encryption = OEELInternal.FUNCTION.newHasher();
		encryption.putUnencodedChars("decrypted output");

		List<String> copy = new ArrayList<>(this.inputs);
		copy.sort(Comparator.naturalOrder());
		for(String s : copy) {
			validation.putUnencodedChars(s);
			encryption.putUnencodedChars(s);
		}

		ObfuscatedShapelessCraftingRecipe shaped = new ObfuscatedShapelessCraftingRecipe(ID,
		                                                                                 validation.hash(),
		                                                                                 this.subst,
		                                                                                 OEELInternal.encryptItem(this.output, encryption.hash()),
		                                                                                 OEELInternal.FUNCTION);
		JsonObject object = new JsonObject();
		object.addProperty("type", "oeel:obf_shapeless");
		ObfuscatedShapelessCraftingRecipe.SERIALIZER.write(object, shaped);
		return object;
	}
}
