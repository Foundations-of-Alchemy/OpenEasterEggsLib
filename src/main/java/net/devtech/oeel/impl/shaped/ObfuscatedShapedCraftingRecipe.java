package net.devtech.oeel.impl.shaped;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.AbstractRecipeSerializer;
import org.jetbrains.annotations.Nullable;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public final class ObfuscatedShapedCraftingRecipe extends AbstractObfuscatedShapedCraftingRecipe {
	public static final AbstractRecipeSerializer<ObfuscatedShapedCraftingRecipe> SERIALIZER = new Serializer();
	final int width;
	final int height;

	public ObfuscatedShapedCraftingRecipe(Identifier id,
			HashCode hash,
			@Nullable Identifier substitutionConfig,
			byte[] output,
			int width,
			int height,
			HashFunction function) {
		super(id, hash, substitutionConfig, output, function);
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean fits(int width, int height) {
		return this.width <= width && this.height <= height;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public ItemStack craft(CraftingInventory inventory, boolean dryRun) throws GeneralSecurityException, IOException {
		for(int offX = 0; offX < inventory.getWidth() - this.width; offX++) {
			for(int offY = 0; offY < inventory.getHeight() - this.height; offY++) {
				ItemStack craft = this.craft(inventory, offX, offY, dryRun);
				if(!craft.isEmpty()) {
					return craft;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public ItemStack craft(CraftingInventory inventory, int offX, int offY, boolean dryRun) throws GeneralSecurityException, IOException {
		Hasher validation = this.function.newHasher(), decryption = this.function.newHasher();
		decryption.putUnencodedChars("decrypted output");
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				ItemKey key = ItemKey.of(inventory.getStack((offX + x) + (offY + y) * inventory.getWidth()));
				this.hasher.hash(key, validation);
				this.hasher.hash(key, decryption);
			}
		}
		if(validation.hash().equals(this.hash)) {
			if(dryRun) {
				return STACK;
			}

			return this.decrypt(decryption.hash());
		} else {
			return ItemStack.EMPTY;
		}
	}

	public static class Serializer extends AbstractObfuscatedShapedCraftingRecipe.Serializer<ObfuscatedShapedCraftingRecipe> {
		@Override
		protected void writeCustom(PacketByteBuf buf, ObfuscatedShapedCraftingRecipe val) {
			buf.writeInt(val.width);
			buf.writeInt(val.height);
		}

		@Override
		protected void writeCustom(JsonObject object, ObfuscatedShapedCraftingRecipe val) {
			JsonArray dimensions = new JsonArray();
			dimensions.add(val.width);
			dimensions.add(val.height);
			object.add("dimensions", dimensions);
		}

		@Override
		protected ObfuscatedShapedCraftingRecipe read(JsonObject json,
				Identifier id,
				HashCode code,
				Identifier substCfg,
				byte[] bytes,
				HashFunction function) {
			JsonArray array = Validate.notNull(json.getAsJsonArray("dimensions"), "no dimensions found!");
			Validate.isTrue(array.size() == 2, "crafting matrix is 2d");
			int width = array.get(0).getAsInt(), height = array.get(1).getAsInt();
			return new ObfuscatedShapedCraftingRecipe(id, code, substCfg, bytes, width, height, function);
		}

		@Override
		protected ObfuscatedShapedCraftingRecipe read(PacketByteBuf buf,
				Identifier id,
				HashCode code,
				Identifier substCfg,
				byte[] bytes,
				HashFunction function) {
			int width = buf.readInt(), height = buf.readInt();
			return new ObfuscatedShapedCraftingRecipe(id, code, substCfg, bytes, width, height, function);
		}
	}
}
