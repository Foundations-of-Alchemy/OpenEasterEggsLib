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
import net.devtech.oeel.impl.OEELInternal;

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
		List<String> items = new ArrayList<>();
		for(int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if(!stack.isEmpty()) {
				String str = OEELInternal.hash(stack, this.function).toString();
				if(this.substitution != null) {
					str = this.substitution.substitute(str, ItemKey.of(stack));
				}
				items.add(str);
			}
		}

		items.sort(Comparator.naturalOrder());

		Hasher validation = this.function.newHasher(), decryption = this.function.newHasher();
		decryption.putUnencodedChars("decrypted output");
		for(String item : items) {
			validation.putUnencodedChars(item);
			decryption.putUnencodedChars(item);
		}

		if(this.hash.equals(validation.hash())) {
			if(dryRun) {
				return STACK;
			}

			return this.decrypt(decryption.hash());
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
