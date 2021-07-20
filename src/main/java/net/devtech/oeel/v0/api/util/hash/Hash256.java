package net.devtech.oeel.v0.api.util.hash;

import java.security.DigestException;
import java.security.MessageDigest;

import com.google.common.hash.HashCode;
import org.jetbrains.annotations.NotNull;

public final class Hash256 extends FixedBuffer<Hash256> {
	long a, b, c, d;

	public Hash256(long a, long b, long c, long d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public Hash256(String base16) {
		int index = 0;
		this.a = Long.parseLong(base16, index += 16, index, 16);
		this.b = Long.parseLong(base16, index += 16, index, 16);
		this.c = Long.parseLong(base16, index += 16, index, 16);
		this.d = Long.parseLong(base16, index += 16, index, 16);
	}

	public Hash256(MessageDigest digest) throws DigestException {
		byte[] data = SmallBuf.COPY.buffer;
		int off = SmallBuf.COPY.getSection();
		digest.digest(data, off, 32);
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
	}

	public Hash256(HashCode code) {
		byte[] data = SmallBuf.COPY.buffer;
		int off = SmallBuf.COPY.getSection();
		code.writeBytesTo(data, off, 32);
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
	}

	public Hash256(byte[] data, int off) {
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
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
	public int compareTo(@NotNull Hash256 o) {
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
		return this == o || o instanceof Hash256 h && this.compareTo(h) == 0;
	}
}
