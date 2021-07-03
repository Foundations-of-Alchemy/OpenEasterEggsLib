package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.EncryptionEntry;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.access.HashSubstitution;
import net.devtech.oeel.v0.api.recipes.BaseObfuscatedRecipe;
import net.devtech.oeel.v0.api.util.BiHasher;
import net.devtech.oeel.v0.api.util.OEELEncrypting;
import net.devtech.oeel.v0.api.util.OEELHashing;
import net.devtech.oeel.v0.api.util.OEELSerializing;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ObfuscatedCraftingRecipeBridge extends SpecialCraftingRecipe {
	public static final Identifier SHAPELESS = new Identifier("oeel:shapeless");
	public static final Identifier SHAPED = new Identifier("oeel:shaped");
	public static final RecipeSerializer<?> SERIALIZER = OEELInternal.bridgeSerializer(ObfuscatedCraftingRecipeBridge::new);

	private static final ItemStack STACK = new ItemStack(Items.STONE);

	public ObfuscatedCraftingRecipeBridge(Identifier id) {
		super(id);
	}

	/**
	 * @param testingForEmpty if true, the output stack is not decrypted, and instead a non-empty stack is returned.
	 */
	public static ItemStack craft(boolean testingForEmpty, Function<HashSubstitution<ItemKey>, EncryptionEntry> hash)
			throws GeneralSecurityException, IOException {
		// perhaps instead just iterate through all item hash substitutions
		for(HashSubstitution<ItemKey> substitution : OEEL.RECIPES.allItemSubstitutions()) {
			EncryptionEntry test = hash.apply(substitution);
			BaseObfuscatedRecipe recipe = OEEL.RECIPES.getForInput(test.validation());
			if(recipe != null) {
				if(testingForEmpty) {
					return STACK;
				} else {
					return OEELSerializing.deserializeItem(OEELEncrypting.decrypt(test.encryption(), recipe.encryptedOutput));
				}
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * @param testingForEmpty if true, the output stack is not decrypted, and instead a non-empty stack is returned.
	 */
	public static ItemStack craftShaped(Identifier shaped,
			Inventory inventory,
			int matrixWidth,
			int matrixHeight,
			int width,
			int height,
			boolean testingForEmpty) {
		try {
			for(int offX = 0; offX <= matrixWidth - width; offX++) {
				for(int offY = 0; offY <= matrixHeight - height; offY++) {
					final int finalOffX = offX;
					final int finalOffY = offY;
					final Function<HashSubstitution<ItemKey>, EncryptionEntry> func;
					func = (s) -> hashMatrix(shaped, s, inventory, matrixWidth, width, height, finalOffX, finalOffY, testingForEmpty);
					ItemStack craft = craft(testingForEmpty, func);
					if(!craft.isEmpty()) {
						// todo ensure that all the other slots are empty
						return craft;
					}
				}
			}
			return ItemStack.EMPTY;
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	/**
	 * Hashes a crafting matrix, this is how shaped recipes are implemented
	 *
	 * @param testingForEmpty if true {@link EncryptionEntry#encryption()} is null
	 */
	public static EncryptionEntry hashMatrix(Identifier id,
			HashSubstitution<ItemKey> substitution,
			Inventory inventory,
			int matrixWidth,
			int width,
			int height,
			int offX,
			int offY,
			boolean testingForEmpty) {
		BiHasher hasher = BiHasher.createDefault(testingForEmpty);
		hasher.putIdentifier(id);
		hasher.putInt(width);
		hasher.putInt(height);

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				ItemStack stack = inventory.getStack((offX + x) + (offY + y) * matrixWidth);
				String str = OEELHashing.hash(stack).toString();
				if(substitution != null) {
					str = substitution.substitute(str, ItemKey.of(stack));
				}
				hasher.putString(str, StandardCharsets.US_ASCII);
			}
		}

		return new EncryptionEntry(hasher);
	}

	/**
	 * @param testingForEmpty if true, the output stack is not decrypted, and instead a non-empty stack is returned.
	 */
	public static ItemStack craftUnshaped(Identifier unshaped, Inventory inventory, boolean testingForEmpty) {
		try {
			return craft(testingForEmpty, substitution -> hashInventoryUnordered(unshaped, substitution, inventory, testingForEmpty));
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	/**
	 * hashes the inventory with no emphasis on order. (for Unshaped recipes)
	 *
	 * @param id a unique id for this type of recipe, eg. oeel:shapeless
	 */
	public static EncryptionEntry hashInventoryUnordered(Identifier id, HashSubstitution<ItemKey> substitution, Inventory inventory, boolean testingForEmpty) {
		List<String> items = new ArrayList<>();
		for(int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if(!stack.isEmpty()) {
				String str = OEELHashing.hash(stack).toString();
				if(substitution != null) {
					str = substitution.substitute(str, ItemKey.of(stack));
				}
				items.add(str);
			}
		}

		items.sort(Comparator.naturalOrder());

		Hasher validation = OEELHashing.FUNCTION.newHasher(), decryption = testingForEmpty ? null : OEELHashing.FUNCTION.newHasher();
		validation.putString(id.getNamespace(), StandardCharsets.US_ASCII);
		validation.putString(id.getPath(), StandardCharsets.US_ASCII);
		if(!testingForEmpty) {
			decryption.putLong(OEELEncrypting.MAGIC);
			decryption.putString(id.getNamespace(), StandardCharsets.US_ASCII);
			decryption.putString(id.getPath(), StandardCharsets.US_ASCII);
		}

		for(String item : items) {
			validation.putString(item, StandardCharsets.US_ASCII);
			if(!testingForEmpty) {
				decryption.putString(item, StandardCharsets.US_ASCII);
			}
		}

		return new EncryptionEntry(validation.hash(), testingForEmpty ? null : decryption.hash());
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		return !craft(inventory, true).isEmpty();
	}

	@Override
	public ItemStack craft(CraftingInventory inventory) {
		return craft(inventory, false);
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static ItemStack craft(CraftingInventory inventory, boolean testingForEmpty) {
		try {
			for(int width = 1; width <= 3; width++) {
				for(int height = 1; height <= 3; height++) {
					ItemStack stack = craftShaped(SHAPED,
					                                                 inventory,
					                                                 inventory.getWidth(),
					                                                 inventory.getHeight(),
					                                                 width,
					                                                 height,
					                                                 testingForEmpty);
					if(!stack.isEmpty()) {
						return stack;
					}
				}
			}
			return craftUnshaped(SHAPELESS, inventory, testingForEmpty);
		} catch(Throwable t) {
			throw Validate.rethrow(t);
		}
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

}
