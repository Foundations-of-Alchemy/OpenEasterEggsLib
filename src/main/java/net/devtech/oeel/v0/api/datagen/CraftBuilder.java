package net.devtech.oeel.v0.api.datagen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.shaped.ObfuscatedShapedCraftingRecipe;
import net.devtech.oeel.impl.shaped.ObfuscatedShapelessCraftingRecipe;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class CraftBuilder {
	private static final Identifier ID = new Identifier("oeel", "empty");
	private final List<ItemStack> inputs = new ArrayList<>();
	private final ItemStack output;
	private Identifier subst;

	public CraftBuilder(ItemStack output) {
		this.output = output;
	}

	public static void main(String[] args) {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
		JsonObject object = CraftBuilder.builder(new ItemStack(Items.STONE)).addItem(Items.STICK).addItem(Items.ITEM_FRAME).shapeless();
		System.out.println(object);
	}

	public JsonObject shapeless() {
		Hasher validation = OEELInternal.FUNCTION.newHasher(), encryption = OEELInternal.FUNCTION.newHasher();
		encryption.putUnencodedChars("decrypted output");

		List<ItemStack> copy = new ArrayList<>(this.inputs);
		copy.sort(Comparator.comparing(CraftBuilder::toString));
		for(ItemStack stack : copy) {
			String s = OEELInternal.hash(stack, OEELInternal.FUNCTION).toString();
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

	/**
	 * add item input, it should be noted however that this matches the itemstack with no nbt at all
	 */
	public CraftBuilder addItem(Item item) {
		return this.addItemStack(new ItemStack(item));
	}

	public static CraftBuilder builder(ItemStack output) {
		return new CraftBuilder(output);
	}

	/**
	 * similar to the ItemKey implementation
	 */
	private static String toString(ItemStack stack) {
		if(stack.hasTag()) {
			return stack.getItem() + " " + stack.getTag();
		} else {
			return stack.getItem().toString();
		}
	}

	/**
	 * does not match count
	 */
	public CraftBuilder addItemStack(ItemStack stack) {
		this.inputs.add(stack);
		return this;
	}

	public CraftBuilder hashSubstitutionConfig(String modid, String path) {
		this.subst = new Identifier(modid, path);
		return this;
	}

	public CraftBuilder hashSubstitutionConfig(Identifier id) {
		this.subst = id;
		return this;
	}

	public JsonObject shaped(int width, int height) {
		Hasher validation = OEELInternal.FUNCTION.newHasher(), encryption = OEELInternal.FUNCTION.newHasher();
		encryption.putUnencodedChars("decrypted output");
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				String s = OEELInternal.hash(this.inputs.get(x * width + y), OEELInternal.FUNCTION).toString();
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
}
