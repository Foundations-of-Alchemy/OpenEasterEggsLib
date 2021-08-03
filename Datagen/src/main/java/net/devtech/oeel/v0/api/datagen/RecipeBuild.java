package net.devtech.oeel.v0.api.datagen;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.impl.shaped.ObfuscatedCraftingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedSmithingRecipeBridge;
import net.devtech.oeel.impl.shaped.ObfuscatedStonecuttingRecipeBridge;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.data.HashFuncComp;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.OEELSerializing;
import net.devtech.oeel.v0.api.util.hash.BiHasher;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import net.devtech.oeel.v0.api.util.hash.Hasher;
import net.devtech.oeel.v0.api.util.hash.SHA256Hasher;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class RecipeBuild {
	private static final Identifier STANDARD = OEELImpl.id("default");

	public final HashFuncComp<ItemKey, Item> hashManager;
	public final BiHasher hasher = BiHasher.createDefault(true);
	public final List<ItemStack> stacks = new ArrayList<>();
	public Identifier hashConfigId = STANDARD;

	protected HashFunction<ItemKey> itemHasher;

	public RecipeBuild(HashFuncComp<ItemKey, Item> manager) {
		this.hashManager = manager;
		if(manager != null) {
			this.itemHasher = this.hashManager.forId(this.hashConfigId);
		}
	}

	public RecipeBuild() {
		this(HashFunctionManager.ITEM_COMP);
		this.itemHasher = Hasher::putItemKey;
	}

	// initialization

	public RecipeBuild stack(ItemStack stack) {
		this.stacks.add(stack);
		return this;
	}

	public RecipeBuild item(Item item) {
		this.stacks.add(new ItemStack(item));
		return this;
	}

	public RecipeBuild setHash(Identifier hashConfigId) {
		this.itemHasher = this.hashManager.forId(hashConfigId);
		return this;
	}

	// hash

	public RecipeBuild shaped(int width) {
		return this.shaped(ObfuscatedCraftingRecipeBridge.SHAPED, width, this.stacks.size() / width);
	}

	public RecipeBuild smithing() {
		return this.fixedSize(ObfuscatedSmithingRecipeBridge.ID);
	}

	public RecipeBuild stonecutting() {
		return this.fixedSize(ObfuscatedStonecuttingRecipeBridge.ID);
	}

	public RecipeBuild shaped(Identifier shapedRecipeId, int width, int height) {
		this.hasher.putIdentifier(shapedRecipeId);
		this.hasher.putInt(width);
		this.hasher.putInt(height);

		for(ItemStack stack : this.stacks) {
			this.itemHasher.hashOrThrow(this.hasher, ItemKey.of(stack));
		}
		return this;
	}

	public RecipeBuild fixedSize(Identifier id) {
		this.hasher.putIdentifier(id);
		for(ItemStack stack : this.stacks) {
			this.itemHasher.hashOrThrow(this.hasher, ItemKey.of(stack));
		}
		return this;
	}

	public RecipeBuild unshaped(Identifier shapelessRecipeId) {
		List<HashKey> items = new ArrayList<>();
		for(ItemStack stack : this.stacks) {
			if(!stack.isEmpty()) {
				try(SHA256Hasher tempHasher = SHA256Hasher.getPooled()) {
					this.itemHasher.hashOrThrow(tempHasher, ItemKey.of(stack));
					items.add(tempHasher.hashC());
				}
			}
		}

		items.sort(Comparator.naturalOrder());

		this.hasher.putIdentifier(shapelessRecipeId);

		for(HashKey item : items) {
			item.hash(this.hasher);
		}
		return this;
	}

	EncryptionEntry key;
	public EncryptionEntry get() {
		if(this.key == null) this.key = this.hasher.hash();
		return this.key;
	}
	// output
	public static byte[] of(ItemStack stack) {
		return OEELSerializing.serializeItem(stack);
	}

	public HashKey writeInDir(Path path, byte[] output) throws IOException {
		EncryptionEntry entry = this.get();
		String str = entry.entryKey().toString().substring(0, 16);
		return write(path.resolve(str + ".data"), output);
	}

	public HashKey write(Path path, byte[] output) throws IOException {
		Files.createDirectories(path.getParent());
		try(OutputStream stream = new BufferedOutputStream(Files.newOutputStream(path))) {
			return this.write(stream, output);
		}
	}

	public HashKey write(OutputStream stream, byte[] output) throws IOException {
		return this.write(this.get(), stream, output);
	}

	public RecipeBuild empty() {
		return this.stack(ItemStack.EMPTY);
	}

	protected HashKey write(EncryptionEntry entry, OutputStream stream, byte[] output) throws IOException {
		try(AbstractEncrypter<?> encrypter = new AbstractEncrypter<>(entry.entryKey(), stream)) {
			encrypter.startEncryptedData(entry.encryptionKey());
			encrypter.writeMagic("oeel:obfr");
			encrypter.writeUTF(this.hashConfigId.toString());
			encrypter.writeUTF("");
			encrypter.writeUTF("");
			encrypter.write(output);
		}
		return entry.entryKey();
	}

}
