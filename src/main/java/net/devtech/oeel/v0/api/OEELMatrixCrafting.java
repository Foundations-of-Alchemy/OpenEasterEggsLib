package net.devtech.oeel.v0.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.v0.api.access.ItemHashSubstitution;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * utilities to help with hashing and crafting with a crafting matrix (eg. survival crafting table / workbench)
 */
public class OEELMatrixCrafting {

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
					final Function<ItemHashSubstitution, EncryptionEntry> func;
					func = (s) -> hashMatrix(shaped, s, inventory, matrixWidth, width, height, finalOffX, finalOffY, testingForEmpty);
					ItemStack craft = ObfuscatedCraftingRecipeBridge.craft(testingForEmpty, func);
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
			ItemHashSubstitution substitution,
			Inventory inventory,
			int matrixWidth,
			int offX,
			int offY,
			int width,
			int height,
			boolean testingForEmpty) {
		Hasher validation = OEELHashing.FUNCTION.newHasher();
		Hasher decryption = testingForEmpty ? null : OEELHashing.FUNCTION.newHasher();

		validation.putString(id.getNamespace(), StandardCharsets.US_ASCII);
		validation.putString(id.getPath(), StandardCharsets.US_ASCII);
		validation.putInt(width);
		validation.putInt(height);

		if(!testingForEmpty) {
			decryption.putLong(OEELEncrypting.MAGIC);
			decryption.putString(id.getNamespace(), StandardCharsets.US_ASCII);
			decryption.putString(id.getPath(), StandardCharsets.US_ASCII);
			decryption.putInt(width);
			decryption.putInt(height);
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				ItemStack stack = inventory.getStack((offX + x) + (offY + y) * matrixWidth);
				String str = OEELHashing.hash(stack).toString();
				if(substitution != null) {
					str = substitution.substitute(str, ItemKey.of(stack));
				}
				validation.putString(str, StandardCharsets.US_ASCII);
				if(!testingForEmpty) {
					decryption.putString(str, StandardCharsets.US_ASCII);
				}
			}
		}

		return new EncryptionEntry(validation.hash(), testingForEmpty ? null : decryption.hash());
	}

	/**
	 * @param testingForEmpty if true, the output stack is not decrypted, and instead a non-empty stack is returned.
	 */
	public static ItemStack craftUnshaped(Identifier unshaped, Inventory inventory, boolean testingForEmpty) {
		try {
			return ObfuscatedCraftingRecipeBridge.craft(testingForEmpty, substitution -> hashInventoryUnordered(unshaped, substitution, inventory, testingForEmpty));
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	/**
	 * hashes the inventory with no emphasis on order. (for Unshaped recipes)
	 *
	 * @param id a unique id for this type of recipe, eg. oeel:shapeless
	 */
	public static EncryptionEntry hashInventoryUnordered(Identifier id, ItemHashSubstitution substitution, Inventory inventory, boolean testingForEmpty) {
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
}
