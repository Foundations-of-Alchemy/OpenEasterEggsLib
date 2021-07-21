package net.devtech.oeel.v0.api.recipes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.util.BlockData;
import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * A base obfuscated recipe
 */
public class BaseObfuscatedRecipe {
	public static final ByteDeserializer<BaseObfuscatedRecipe> SERIALIZER = new Deserializer<>(BaseObfuscatedRecipe::new, "oeel/obf_r");
	protected HashFunction<ItemKey> item;
	protected HashFunction<Entity> entity;
	protected HashFunction<BlockData> block;
	private byte[] output;
	@Nullable private Identifier itemHashFunctionId;
	@Nullable private Identifier entityHashFunctionId;
	@Nullable private Identifier blockHashFunctionId;

	protected BaseObfuscatedRecipe() {}

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
			this.item = item = HashFunctionManager.ITEM_COMP.forId(id);
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
			this.entity = entity = HashFunctionManager.ENTITY_COMP.forId(id);
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
			this.block = block = HashFunctionManager.BLOCK_COMP.forId(id);
		}
		return block;
	}

	/**
	 * the decrypted output
	 */
	public byte[] getOutput() {
		return this.output;
	}

	public static Identifier readIdentifier(DataInputStream buffer) throws IOException {
		long packedName = buffer.readLong();
		if(packedName == 0) {
			return null;
		}
		long packedPath = buffer.readLong();
		String name, path;
		if(packedName != -1) {
			name = IdentifierPacker.unpack(packedName);
		} else {
			name = buffer.readUTF();
		}

		if(packedPath != -1) {
			path = IdentifierPacker.unpack(packedPath);
		} else {
			path = buffer.readUTF();
		}

		return new Identifier(name, path);
	}


	private static class Deserializer<T extends BaseObfuscatedRecipe> implements ByteDeserializer<T> {
		public final Supplier<T> newInstance;
		public final String magic;

		private Deserializer(Supplier<T> instance, String magic) {
			this.newInstance = instance;
			this.magic = magic;
		}

		@Override
		public String magic() {
			return this.magic;
		}

		@Override
		public T newInstance() {
			return this.newInstance.get();
		}

		@Override
		public void read(BaseObfuscatedRecipe instance, DataInputStream buffer, HashKey inputHash) throws IOException {
			instance.itemHashFunctionId = readIdentifier(buffer);
			instance.blockHashFunctionId = readIdentifier(buffer);
			instance.entityHashFunctionId = readIdentifier(buffer);
			instance.output = buffer.readNBytes(buffer.readInt());
		}
	}
}
