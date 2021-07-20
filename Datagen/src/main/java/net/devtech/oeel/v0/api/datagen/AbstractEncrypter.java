package net.devtech.oeel.v0.api.datagen;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.devtech.oeel.impl.OEELInternal;
import net.devtech.oeel.v0.api.util.hash.HashKey;

/**
 * u%N = unsigned n bit integer, this is java so most of it is actually implemented as signed integers but meh
 *
 * data: [s4 data] [u1 x data] // an arbitrary length of data
 * string: {data}
 * pkdid: [u8 packedName] [u8 packedPath] if(packedName == -1) {[string name]} if(packedName == -1) {[string path]}
 *
 * sprite: [u32 hash] {[u8 magic] [u4 offX] [u4 offY] [u4 data] [u1 x data]}
 * recipe: [u32 inputHash] {[pkdid itemHasher] [pkdid blockHasher] [pkdid entityHasher] [data output]}
 * @param <Self>
 */
public abstract class AbstractEncrypter<Self extends AbstractEncrypter<Self>> extends DataOutputStream {
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
}
