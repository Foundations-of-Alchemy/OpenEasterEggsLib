package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.EncryptionEntry;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.OEELEncrypting;
import net.devtech.oeel.v0.api.OEELMatrixCrafting;
import net.devtech.oeel.v0.api.OEELSerializing;
import net.devtech.oeel.v0.api.access.ItemHashSubstitution;
import net.devtech.oeel.v0.api.recipes.ObfuscatedItemRecipe;

import net.minecraft.inventory.CraftingInventory;
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
	public static ItemStack craft(boolean testingForEmpty, Function<ItemHashSubstitution, EncryptionEntry> hash)
			throws GeneralSecurityException, IOException {
		Map<Identifier, EncryptionEntry> cache = new HashMap<>();
		for(ObfuscatedItemRecipe recipe : OEEL.ITEM_RECIPES.getAll()) {
			EncryptionEntry test = cache.get(recipe.substitution);
			if(test == null) {
				test = hash.apply(recipe.subst);
				cache.put(recipe.substitution, test);

				// minor optimization
				ObfuscatedItemRecipe immediateMatch = OEEL.ITEM_RECIPES.getForInput(test.validation());
				if(immediateMatch != null) {
					if(testingForEmpty) {
						return STACK;
					} else {
						return OEELSerializing.deserializeItem(OEELEncrypting.decrypt(test.encryption(), immediateMatch.encryptedOutput));
					}
				}
			}

			if(recipe.inputHash.equals(test.validation())) {
				if(testingForEmpty) {
					return STACK;
				} else {
					return OEELSerializing.deserializeItem(OEELEncrypting.decrypt(test.encryption(), recipe.encryptedOutput));
				}
			}
		}

		return ItemStack.EMPTY;
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
					ItemStack stack = OEELMatrixCrafting.craftShaped(SHAPED,
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
			return OEELMatrixCrafting.craftUnshaped(SHAPELESS, inventory, testingForEmpty);
		} catch(Throwable t) {
			throw Validate.rethrow(t);
		}
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

}
