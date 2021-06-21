package net.devtech.oeel.v0.api.datagen;

import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.impl.shaped.ObfuscatedShapedCraftingRecipe;
import net.devtech.oeel.impl.shaped.ObfuscatedShapelessCraftingRecipe;

import net.minecraft.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class CraftBuilder {
	private static final Identifier ID = new Identifier("oeel", "empty");
	private final Hasher validation = OEELInternal.FUNCTION.newHasher(), encryption = OEELInternal.FUNCTION.newHasher();
	private final ItemStack output;
	private Identifier subst;

	public CraftBuilder(ItemStack output) {this.output = output;}

	public static void main(String[] args) {
		Bootstrap.initialize();
		JsonObject object = CraftBuilder.builder(new ItemStack(Items.STONE)).addItem(Items.STICK).addItem(Items.ITEM_FRAME).shapeless();
		System.out.println(object);
	}

	public JsonObject shapeless() {
		ObfuscatedShapelessCraftingRecipe shaped = new ObfuscatedShapelessCraftingRecipe(ID,
		                                                                                 this.validation.hash(),
		                                                                                 this.subst,
		                                                                                 OEELInternal.encryptItem(this.output,
		                                                                                                          this.encryption.hash()),
		                                                                                 OEELInternal.FUNCTION);
		JsonObject object = new JsonObject();
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
	 * does not match count
	 */
	public CraftBuilder addItemStack(ItemStack stack) {
		String s = OEELInternal.hash(stack, OEELInternal.FUNCTION).toString();
		this.validation.putUnencodedChars(s);
		this.encryption.putUnencodedChars(s);
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
		ObfuscatedShapedCraftingRecipe shaped = new ObfuscatedShapedCraftingRecipe(ID,
		                                                                           this.validation.hash(),
		                                                                           this.subst,
		                                                                           OEELInternal.encryptItem(this.output, this.encryption.hash()),
		                                                                           width,
		                                                                           height,
		                                                                           OEELInternal.FUNCTION);
		JsonObject object = new JsonObject();
		ObfuscatedShapedCraftingRecipe.SERIALIZER.write(object, shaped);
		return object;
	}
}
