package net.devtech.oeel.impl.shaped;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.impl.AbstractRecipeSerializer;
import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.access.ItemHasher;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class AbstractObfuscatedShapedCraftingRecipe extends SpecialCraftingRecipe {
	public static final ThreadLocal<byte[]> BUFFERS = ThreadLocal.withInitial(() -> new byte[1024]);
	protected static final ItemStack STACK = new ItemStack(Items.STONE);
	final HashCode hash;
	final Identifier substitutionConfig;
	final ItemHasher hasher;
	final byte[] output;
	final HashFunction function;

	public AbstractObfuscatedShapedCraftingRecipe(Identifier id, HashCode hash, Identifier config, byte[] output, HashFunction function) {
		super(id);
		this.hash = hash;
		this.substitutionConfig = config;
		this.output = output;
		this.function = function;
		this.hasher = OEELInternal.from(substitutionConfig);
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		try {
			return !this.craft(inventory, true).isEmpty();
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	@Override
	public ItemStack craft(CraftingInventory inventory) {
		try {
			return this.craft(inventory, false);
		} catch(GeneralSecurityException | IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public abstract ItemStack craft(CraftingInventory inventory, boolean dryRun) throws GeneralSecurityException, IOException;

	protected ItemStack decrypt(HashCode key) throws GeneralSecurityException, IOException {
		return OEELInternal.decryptItem(key, this.output);
	}

	protected static abstract class Serializer<T extends AbstractObfuscatedShapedCraftingRecipe> implements AbstractRecipeSerializer<T> {
		@Override
		public T read(Identifier id, JsonObject json) {
			String inputHash = Validate.notNull(json.getAsJsonPrimitive("input"), "No input hash!").getAsString();
			byte[] inputHashBytes = OEELInternal.decodeBase16(inputHash);
			HashCode inputHashCode = HashCode.fromBytes(inputHashBytes);

			Identifier substCfgId = Validate.transform(Validate.transform(json.getAsJsonPrimitive("substitutionConfig"), JsonPrimitive::getAsString),
			                                           Identifier::new);

			String output = Validate.notNull(json.getAsJsonPrimitive("output"), "No output item!").getAsString();
			byte[] outputBytes = OEELInternal.decodeBase16(output);

			return this.read(json, id, inputHashCode, substCfgId, outputBytes, Hashing.sha256());
		}


		@Override
		public void write(JsonObject object, T recipe) {
			object.addProperty("input", recipe.hash.toString());
			if(recipe.substitutionConfig != null) {
				object.addProperty("substitutionConfig", recipe.substitutionConfig.toString());
			}
			object.addProperty("output", OEELInternal.encodeBase16(recipe.output));
			this.writeCustom(object, recipe);
		}

		@Override
		public T read(Identifier id, PacketByteBuf buf) {
			byte[] hash = buf.readByteArray();
			Identifier substCfg = buf.readBoolean() ? buf.readIdentifier() : null;
			byte[] output = buf.readByteArray();
			return this.read(buf, id, HashCode.fromBytes(hash), substCfg, output, Hashing.sha256());
		}

		@Override
		public void write(PacketByteBuf buf, T recipe) {
			byte[] buffer = BUFFERS.get();
			int hashSize = Math.min(recipe.hash.bits() / 8, buffer.length);
			// input
			buf.writeInt(hashSize);
			recipe.hash.writeBytesTo(buffer, 0, hashSize);
			buf.writeBytes(buffer, 0, hashSize);
			// subst
			Identifier id = recipe.substitutionConfig;
			buf.writeBoolean(id != null);
			if(id != null) {
				buf.writeIdentifier(id);
			}
			// output
			buf.writeByteArray(recipe.output);
			// dimensions
			this.writeCustom(buf, recipe);
		}

		protected abstract void writeCustom(PacketByteBuf buf, T val);

		protected abstract void writeCustom(JsonObject object, T val);

		protected abstract T read(JsonObject json, Identifier recipeId, HashCode code, Identifier substCfg, byte[] bytes, HashFunction function);
		protected abstract T read(PacketByteBuf buf, Identifier recipeId, HashCode code, Identifier substCfg, byte[] bytes, HashFunction function);
	}
}
