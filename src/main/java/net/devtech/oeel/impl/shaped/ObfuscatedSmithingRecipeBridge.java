package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.EncryptionEntry;
import net.devtech.oeel.v0.api.OEELEncrypting;
import net.devtech.oeel.v0.api.OEELHashing;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ObfuscatedSmithingRecipeBridge extends SmithingRecipe {
	public static final Identifier ID = new Identifier("oeel:obf_smithing");
	public static final RecipeSerializer<?> SERIALIZER = OEELInternal.bridgeSerializer(ObfuscatedSmithingRecipeBridge::new);
	public static final ItemStack ICON;

	static {
		ItemStack stack = new ItemStack(Items.SMITHING_TABLE);
		Map<Enchantment, Integer> enchantments = new HashMap<>();
		enchantments.put(Enchantments.UNBREAKING, 1);
		EnchantmentHelper.set(enchantments, stack);
		stack.getOrCreateTag().putInt("HideFlags", 1);
		ICON = stack;
	}

	public ObfuscatedSmithingRecipeBridge(Identifier id) {
		super(id, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		return !craft(inventory, true).isEmpty();
	}

	@Override
	public ItemStack craft(Inventory inventory) {
		return craft(inventory, false);
	}

	public static ItemStack craft(Inventory inventory, boolean testingForEmpty) {
		try {
			return ObfuscatedCraftingRecipeBridge.craft(testingForEmpty, substitution -> {
				Hasher validation = OEELHashing.FUNCTION.newHasher();
				Hasher decryption = testingForEmpty ? null : OEELHashing.FUNCTION.newHasher();

				validation.putString(ID.getNamespace(), StandardCharsets.US_ASCII);
				validation.putString(ID.getPath(), StandardCharsets.US_ASCII);

				if(!testingForEmpty) {
					decryption.putLong(OEELEncrypting.MAGIC);
					decryption.putString(ID.getNamespace(), StandardCharsets.US_ASCII);
					decryption.putString(ID.getPath(), StandardCharsets.US_ASCII);
				}

				for(int i = 0; i < 2; i++) {
					ItemStack stack = inventory.getStack(i);
					String str = OEELHashing.hash(stack).toString();
					if(substitution != null) {
						str = substitution.substitute(str, ItemKey.of(stack));
					}
					validation.putString(str, StandardCharsets.US_ASCII);
					if(!testingForEmpty) {
						decryption.putString(str, StandardCharsets.US_ASCII);
					}
				}

				return new EncryptionEntry(validation.hash(), testingForEmpty ? null : decryption.hash());
			});
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public boolean testAddition(ItemStack stack) {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}
}
