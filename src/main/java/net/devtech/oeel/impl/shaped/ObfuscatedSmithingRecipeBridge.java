package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.v0.api.util.hash.BiHasher;

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
	public static final RecipeSerializer<?> SERIALIZER = OEELImpl.bridgeSerializer(ObfuscatedSmithingRecipeBridge::new);
	public static final ItemStack ICON;

	static {
		ItemStack stack = new ItemStack(Items.SMITHING_TABLE);
		Map<Enchantment, Integer> enchantments = new HashMap<>();
		enchantments.put(Enchantments.UNBREAKING, 1);
		EnchantmentHelper.set(enchantments, stack);
		stack.getOrCreateNbt().putInt("HideFlags", 1);
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
			return ObfuscatedCraftingRecipeBridge.craft(testingForEmpty, function -> {
				try(BiHasher hasher = BiHasher.createDefault(testingForEmpty)) {
					hasher.putIdentifier(ID);
					for(int i = 0; i < 2; i++) {
						ItemStack stack = inventory.getStack(i);
						function.hashOrThrow(hasher, ItemKey.of(stack));
					}
					return hasher.hash();
				}
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
