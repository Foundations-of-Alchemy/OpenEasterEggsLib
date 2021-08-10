package net.devtech.oeel.v0.api.datagen;

import java.io.IOException;
import java.io.OutputStream;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.client.OEELModelOverrideList;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.hash.BiHasher;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ModelEncrypter {
	public static void encrypt(String json, OutputStream stream, ItemStack stack, Identifier hashFunction) throws IOException {
		HashFunction<ItemKey> function = HashFunctionManager.CLIENT.itemComp.forId(hashFunction);
		EncryptionEntry entry;
		try(BiHasher hasher = BiHasher.createDefault(true)) {
			hasher.putIdentifier(OEELModelOverrideList.ID);
			function.hash(hasher, ItemKey.of(stack));
			entry = hasher.hash();
		}

		try(AbstractEncrypter<?> encrypter = new AbstractEncrypter<>(entry.entryKey(), stream)) {
			encrypter.startEncryptedData(entry.encryptionKey());
			encrypter.writeUTF(hashFunction.getPath());
			encrypter.writeUTF(hashFunction.getNamespace());
			encrypter.writeUTF(json);
		}
	}
}
