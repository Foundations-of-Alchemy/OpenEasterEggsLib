package net.devtech.oeel.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.access.ItemHasher;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtIo;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public final class ObfuscatedShapedCraftingRecipe extends SpecialCraftingRecipe {
	public static final String ALGORITHM = "AES";
	private static final ItemStack STACK = new ItemStack(Items.STONE);
	final HashCode hash;
	final ItemHasher hasher;
	final String output;
	final int width;
	final int height;
	final HashFunction function;

	public ObfuscatedShapedCraftingRecipe(Identifier id,
			HashCode hash,
			ItemHasher hasher,
			String output,
			int width,
			int height,
			HashFunction function) {
		super(id);
		this.hash = hash;
		this.hasher = hasher;
		this.output = output;
		this.width = width;
		this.height = height;
		this.function = function;
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		return !this.craft(inventory, true).isEmpty();
	}

	@Override
	public ItemStack craft(CraftingInventory inventory) {
		return this.craft(inventory, false);
	}

	@Override
	public boolean fits(int width, int height) {
		return this.width <= width && this.height <= height;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null; // todo
	}

	public ItemStack craft(CraftingInventory inventory, boolean dryRun) {
		try {
			for(int offX = 0; offX < inventory.getWidth() - this.width; offX++) {
				for(int offY = 0; offY < inventory.getHeight() - this.height; offY++) {
					ItemStack craft = this.craft(inventory, offX, offY, dryRun);
					if(!craft.isEmpty()) {
						return craft;
					}
				}
			}
			return ItemStack.EMPTY;
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public ItemStack craft(CraftingInventory inventory, int offX, int offY, boolean dryRun) throws GeneralSecurityException, IOException {
		Hasher validation = this.function.newHasher(), decryption = this.function.newHasher();
		decryption.putUnencodedChars("decrypted output");
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				ItemKey key = ItemKey.of(inventory.getStack(offX + x + (offY + y) * inventory.getWidth()));
				this.hasher.hash(key, validation);
				this.hasher.hash(key, decryption);
			}
		}
		if(validation.hash().equals(this.hash)) {
			if(dryRun) {
				return STACK;
			}

			byte[] keyBytes = new byte[128];
			decryption.hash().writeBytesTo(keyBytes, 0, keyBytes.length);
			byte[] itemBytes = decrypt(keyBytes, Base64.getDecoder().decode(this.output));
			ByteArrayInputStream bis = new ByteArrayInputStream(itemBytes);
			DataInputStream dis = new DataInputStream(bis);
			ItemStack stack = ItemStack.fromNbt(NbtIo.read(dis));

			for(int x = 0; x < this.width; x++) {
				for(int y = 0; y < this.height; y++) {
					ItemStack at = inventory.getStack(offX + x + (offY + y) * inventory.getWidth());
					if(!at.isEmpty()) {
						at.decrement(1);
					}
				}
			}

			return stack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	public static byte[] decrypt(byte[] keyBytes, byte[] inputBytes) throws GeneralSecurityException {
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decorVal = Base64.getDecoder().decode(inputBytes);
		return c.doFinal(decorVal);
	}
}
