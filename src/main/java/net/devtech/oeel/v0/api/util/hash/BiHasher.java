package net.devtech.oeel.v0.api.util.hash;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.devtech.oeel.v0.api.EncryptionEntry;

public class BiHasher extends AbstractHasher implements Closeable {
	private static final byte[] MAGIC = ByteBuffer.allocate(8).putLong(0xDEAD_BEEF_CAFE_BABEL).array();
	public final SHA256Hasher hasherA, hasherB;

	public BiHasher(SHA256Hasher hasherA, SHA256Hasher hasherB) {
		this.hasherA = hasherA;
		this.hasherB = hasherB;
	}

	public static BiHasher createDefault(boolean needsSecond) {
		SHA256Hasher a = SHA256Hasher.getPooled();
		SHA256Hasher b = null;
		if(needsSecond) {
			b = SHA256Hasher.getPooled();
			b.putBytes(MAGIC, 0, 8);
		}
		return new BiHasher(a, b);
	}

	@Override
	public void putByte0(byte b) {
		if(this.hasherA != null) {
			this.hasherA.putByte(b);
		}
		if(this.hasherB != null) {
			this.hasherB.putByte(b);
		}
	}

	@Override
	public void putBytes0(byte[] bytes, int off, int len) {
		if(this.hasherA != null) {
			this.hasherA.putBytes(bytes, off, len);
		}
		if(this.hasherB != null) {
			this.hasherB.putBytes(bytes, off, len);
		}
	}

	public byte[] hashB() {
		return this.hasherB.hash();
	}

	@Override
	protected OutputStream createOutputStream0() {
		return new OutputStream() {
			final OutputStream a = BiHasher.this.hasherA.asOutputStream();
			final OutputStream b = BiHasher.this.hasherA.asOutputStream();

			@Override
			public void write(int b) throws IOException {
				this.a.write(b);
				this.b.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				this.a.write(b, off, len);
				this.b.write(b, off, len);
			}
		};
	}

	@Override
	public void close() {
		if(this.hasherA != null) {
			this.hasherA.close();
		}
		if(this.hasherB != null) {
			this.hasherB.close();
		}
	}

	public EncryptionEntry hash() {
		if(this.hasherB == null) {
			return new EncryptionEntry(this.hasherA.hashCompact(), null);
		} else {
			return new EncryptionEntry(this.hasherA.hashCompact(), this.hasherB.hash());
		}
	}

	public HashKey hashA() {
		return this.hasherA.hashCompact();
	}

}
