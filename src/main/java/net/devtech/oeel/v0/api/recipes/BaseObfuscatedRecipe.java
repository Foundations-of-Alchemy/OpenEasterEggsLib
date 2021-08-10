package net.devtech.oeel.v0.api.recipes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.access.OEELDeserializer;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * A base obfuscated recipe
 */
public class BaseObfuscatedRecipe {
	protected HashFunction<ItemKey> item;
	protected HashFunction<Entity> entity;
	protected HashFunction<BlockData> block;
	private final byte[] output;
	@Nullable private final Identifier itemHashFunctionId;
	@Nullable private final Identifier entityHashFunctionId;
	@Nullable private final Identifier blockHashFunctionId;

	public BaseObfuscatedRecipe(DataInputStream buffer, HashKey key) throws IOException {
		this.itemHashFunctionId = rni(buffer.readUTF());
		this.blockHashFunctionId = rni(buffer.readUTF());
		this.entityHashFunctionId = rni(buffer.readUTF());
		this.output = buffer.readAllBytes();
	}

	public BaseObfuscatedRecipe(byte[] output,
			@Nullable Identifier itemHashFunctionId,
			@Nullable Identifier entityHashFunctionId,
			@Nullable Identifier blockHashFunctionId) {
		this.output = output;
		this.itemHashFunctionId = itemHashFunctionId;
		this.entityHashFunctionId = entityHashFunctionId;
		this.blockHashFunctionId = blockHashFunctionId;
	}

	public boolean isValid(Identifier itemHasherId, Identifier blockHasherId, Identifier entityHasherId) {
		return Objects.equals(itemHasherId, this.itemHashFunctionId) && Objects.equals(entityHasherId, this.entityHashFunctionId) && Objects.equals(
				blockHasherId,
				this.blockHashFunctionId);
	}

	public HashFunction<ItemKey> getItemHashFunction() {
		HashFunction<ItemKey> item = this.item;
		if(item == null) {
			Identifier id = this.itemHashFunctionId;
			if(id == null) {
				return null;
			}
			this.item = item = HashFunctionManager.SERVER.itemComp.forId(id);
		}
		return item;
	}

	public HashFunction<Entity> getEntityHashFunction() {
		HashFunction<Entity> entity = this.entity;
		if(entity == null) {
			Identifier id = this.entityHashFunctionId;
			if(id == null) {
				return null;
			}
			this.entity = entity = HashFunctionManager.SERVER.entityComp.forId(id);
		}
		return entity;
	}

	public HashFunction<BlockData> getBlockHashFunction() {
		HashFunction<BlockData> block = this.block;
		if(block == null) {
			Identifier id = this.blockHashFunctionId;
			if(id == null) {
				return null;
			}
			this.block = block = HashFunctionManager.SERVER.blockComp.forId(id);
		}
		return block;
	}

	/**
	 * the decrypted output
	 */
	public byte[] getOutput() {
		return this.output;
	}

	static Identifier rni(String str) {
		if(str.isEmpty()) return null;
		return new Identifier(str);
	}

}
