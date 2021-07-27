package net.devtech.oeel.v0.api.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.github.astrarre.util.v0.api.Validate;
import net.devtech.oeel.v0.api.util.hash.HashKey;

import net.minecraft.util.Util;

public record EncryptionEntry(HashKey entryKey, byte[] encryptionKey) {
	public EncryptionEntry(DataInputStream input) throws IOException {
		this(new HashKey((DataInput) input), Util.make(() -> {
			try {
				byte[] read = new byte[input.readInt()];
				Validate.isTrue(input.read(read) == read.length, "EOF");
				return read;
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		}));
	}

	public void write(DataOutputStream buffer) throws IOException {
		this.entryKey.write(buffer);
		buffer.writeInt(this.encryptionKey.length);
		buffer.write(this.encryptionKey);
	}
}