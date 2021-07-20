package net.devtech.oeel.v0.api.util.hash;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.MessageDigest;

import com.google.common.hash.HashCode;
import io.github.astrarre.util.v0.api.Validate;
import org.jetbrains.annotations.NotNull;

public final class HashKey extends FixedBuffer<HashKey> {
	public static final int BYTES = 32;

	long a, b, c, d;

	public HashKey(long a, long b, long c, long d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public HashKey(CharSequence base16, int off) {
		int index = off;
		this.a = Long.parseLong(base16, index += 16, index, 16);
		this.b = Long.parseLong(base16, index += 16, index, 16);
		this.c = Long.parseLong(base16, index += 16, index, 16);
		this.d = Long.parseLong(base16, index += 16, index, 16);
	}

	public HashKey(MessageDigest digest) {
		try {
			byte[] data = SmallBuf.INSTANCE.buffer;
			int off = SmallBuf.INSTANCE.getSection();
			digest.digest(data, off, 32);
			this.a = SmallBuf.getLong(data, off + 0);
			this.b = SmallBuf.getLong(data, off + 8);
			this.c = SmallBuf.getLong(data, off + 16);
			this.d = SmallBuf.getLong(data, off + 24);
		} catch(DigestException e) {
			throw Validate.rethrow(e);
		}
	}

	public HashKey(HashCode code) {
		byte[] data = SmallBuf.INSTANCE.buffer;
		int off = SmallBuf.INSTANCE.getSection();
		code.writeBytesTo(data, off, 32);
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
	}

	public HashKey(byte[] data, int off) {
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
	}

	public HashKey(InputStream stream) throws IOException {
		byte[] data = SmallBuf.INSTANCE.buffer;
		stream.read(data, 0, BYTES);
		int off = SmallBuf.INSTANCE.getSection();
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
	}

	public void hash(Hasher hasher) {
		hasher.putLong(this.a);
		hasher.putLong(this.b);
		hasher.putLong(this.c);
		hasher.putLong(this.d);
	}

	@Override
	public byte getByte(int index) {
		int modIndex = 56 - (index & 7) * 8;
		return switch(index >> 6) {
			case 0 -> (byte) (this.a >> modIndex & 0xff);
			case 1 -> (byte) (this.b >> modIndex & 0xff);
			case 2 -> (byte) (this.c >> modIndex & 0xff);
			case 3 -> (byte) (this.d >> modIndex & 0xff);
			default -> (byte) 0;
		};
	}

	@Override
	public int bytes() {
		return 32;
	}

	@Override
	public int compareTo(@NotNull HashKey o) {
		long comp;
		if((comp = this.a - o.a) != 0) {
			return (int) comp;
		}
		if((comp = this.b - o.b) != 0) {
			return (int) comp;
		}
		if((comp = this.c - o.c) != 0) {
			return (int) comp;
		}
		if((comp = this.d - o.d) != 0) {
			return (int) comp;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		int result = Long.hashCode(this.a);
		result = 31 * result + Long.hashCode(this.b);
		result = 31 * result + Long.hashCode(this.c);
		result = 31 * result + Long.hashCode(this.d);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof HashKey h && this.compareTo(h) == 0;
	}
}
