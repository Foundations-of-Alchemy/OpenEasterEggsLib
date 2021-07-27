package net.devtech.oeel.v0.api.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.util.Util;

public record EncryptionEntry(HashKey entryKey, byte[] encryptionKey) {
	public EncryptionEntry(ByteBuffer input) throws IOException {
		this(new HashKey(input), Util.make(() -> {
			byte[] read = new byte[input.getInt()];
			input.get(read);
			return read;
		}));
	}

	public void write(DataOutputStream buffer) throws IOException {
		this.entryKey.write(buffer);
		buffer.writeInt(this.encryptionKey.length);
		buffer.write(this.encryptionKey);
	}
}