package net.devtech.oeel.v0.api.recipes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.hash.HashCode;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.access.ByteDeserializer;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.util.BlockData;
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
	private HashCode inputHash;
	private byte[] encryptedOutput;
	@Nullable private Identifier itemHashFunctionId;
	@Nullable private Identifier entityHashFunctionId;
	@Nullable private Identifier blockHashFunctionId;

	protected BaseObfuscatedRecipe() {}

	public BaseObfuscatedRecipe(HashCode inputHash,
			byte[] encryptedOutput,
			@Nullable Identifier itemHashFunctionId,
			@Nullable Identifier entityHashFunctionId,
			@Nullable Identifier blockHashFunctionId) {
		this.inputHash = inputHash;
		this.encryptedOutput = encryptedOutput;
		this.itemHashFunctionId = itemHashFunctionId;
		this.entityHashFunctionId = entityHashFunctionId;
		this.blockHashFunctionId = blockHashFunctionId;
	}

	public boolean isValid(HashCode code, Identifier itemHasherId, Identifier blockHasherId, Identifier entityHasherId) {
		return this.inputHash.equals(code) && Objects.equals(itemHasherId, this.itemHashFunctionId) && Objects.equals(entityHasherId, this.entityHashFunctionId) && Objects.equals(blockHasherId, this.blockHashFunctionId);
	}

	public HashFunction<ItemKey> getItemHashFunction() {
		HashFunction<ItemKey> item = this.item;
		if(item == null) {
			Identifier id = this.itemHashFunctionId;
			if(id == null) {
				return null;
			}
			this.item = item = HashFunctionManager.item(id);
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
			this.entity = entity = HashFunctionManager.entity(id);
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
			this.block = block = HashFunctionManager.block(id);
		}
		return block;
	}

	public HashCode getInputHash() {
		return this.inputHash;
	}

	public byte[] getEncryptedOutput() {
		return this.encryptedOutput;
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
		public void read(BaseObfuscatedRecipe instance, ByteBuffer buffer, HashCode inputHash) {
			instance.inputHash = inputHash;
			byte[] encryptedOutput = new byte[buffer.getInt()];
			buffer.get(encryptedOutput);
			instance.encryptedOutput = encryptedOutput;
			instance.itemHashFunctionId = readIdentifier(buffer);
			instance.blockHashFunctionId = readIdentifier(buffer);
			instance.entityHashFunctionId = readIdentifier(buffer);
		}
	}

	public static Identifier readIdentifier(ByteBuffer buffer) {
		int len = buffer.getInt();
		if(len == 0) {
			return null;
		}
		int currentLimit = buffer.limit();
		int endPos = buffer.position() + len;
		try {
			buffer.limit(len);
			String decoded = StandardCharsets.US_ASCII.decode(buffer).toString();
			return new Identifier(decoded);
		} finally {
			buffer.limit(currentLimit);
			buffer.position(endPos);
		}
	}
}
