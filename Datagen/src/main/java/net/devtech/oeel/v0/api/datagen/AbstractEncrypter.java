package net.devtech.oeel.v0.api.datagen;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.hash.HashKey;

/**
 * @param <Self>
 */
public class AbstractEncrypter<Self extends AbstractEncrypter<Self>> extends DataOutputStream {
	@SuppressWarnings("unchecked")
	protected Self t = (Self) this;

	public AbstractEncrypter(HashKey key, OutputStream out) throws IOException {
		super(out);
		this.writeHash(key);
	}

	public Self writeHash(HashKey key) throws IOException {
		for(int i = 0; i < key.longSize(); i++) {
			this.writeLong(key.getLong(i));
		}
		return this.t;
	}

	public Self startEncryptedData(byte[] encryptionKey) {
		this.out = OEELInternal.encryptStream(encryptionKey, this.out);
		return this.t;
	}

	public Self writeMagic(String magic) throws IOException {
		this.writeLong(IdentifierPacker.pack(magic));
		return this.t;
	}
}
