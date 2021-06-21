package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.gson.JsonObject;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.AbstractRecipeSerializer;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class ObfuscatedShapelessCraftingRecipe extends AbstractObfuscatedShapedCraftingRecipe {
	public static final AbstractRecipeSerializer<ObfuscatedShapelessCraftingRecipe> SERIALIZER = new Serializer();

	public ObfuscatedShapelessCraftingRecipe(Identifier id, HashCode hash, Identifier config, byte[] output, HashFunction function) {
		super(id, hash, config, output, function);
	}

	@Override
	public ItemStack craft(CraftingInventory inventory, boolean dryRun) throws GeneralSecurityException, IOException {
		List<ItemKey> items = new ArrayList<>();
		for(int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if(!stack.isEmpty()) {
				items.add(ItemKey.of(stack));
			}
		}

		items.sort(Comparator.comparing(ItemKey::toString));

		Hasher validation = this.function.newHasher(), decryption = this.function.newHasher();
		decryption.putUnencodedChars("decrypted output");
		for(ItemKey item : items) {
			this.hasher.hash(item, validation);
			this.hasher.hash(item, decryption);
		}

		if(this.hash.equals(validation.hash())) {
			if(dryRun) {
				return STACK;
			}

			ItemStack output = this.decrypt(decryption.hash());
			for(int i = 0; i < inventory.size(); i++) {
				ItemStack stack = inventory.getStack(i);
				if(!stack.isEmpty()) {
					ItemStack copy = stack.copy();
					copy.decrement(1);
					inventory.setStack(i, copy);
				}
			}
			return output;
		}
		return ItemStack.EMPTY;
	}

	public static class Serializer extends AbstractObfuscatedShapedCraftingRecipe.Serializer<ObfuscatedShapelessCraftingRecipe> {
		@Override
		protected void writeCustom(PacketByteBuf buf, ObfuscatedShapelessCraftingRecipe val) {
		}

		@Override
		protected void writeCustom(JsonObject object, ObfuscatedShapelessCraftingRecipe val) {
		}

		@Override
		protected ObfuscatedShapelessCraftingRecipe read(JsonObject json,
				Identifier recipeId,
				HashCode code,
				Identifier substCfg,
				byte[] bytes,
				HashFunction function) {
			return new ObfuscatedShapelessCraftingRecipe(recipeId, code, substCfg, bytes, function);
		}

		@Override
		protected ObfuscatedShapelessCraftingRecipe read(PacketByteBuf buf,
				Identifier recipeId,
				HashCode code,
				Identifier substCfg,
				byte[] bytes,
				HashFunction function) {
			return new ObfuscatedShapelessCraftingRecipe(recipeId, code, substCfg, bytes, function);
		}
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
