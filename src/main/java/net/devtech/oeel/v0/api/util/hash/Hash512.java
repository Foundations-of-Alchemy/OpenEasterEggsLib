package net.devtech.oeel.v0.api.util.hash;

import java.security.DigestException;
import java.security.MessageDigest;

import com.google.common.hash.HashCode;
import net.devtech.oeel.impl.OEELInternal;
import org.jetbrains.annotations.NotNull;

public class Hash512 extends FixedBuffer<Hash512> {
	long a, b, c, d, e, f, g, h;

	public Hash512(long a, long b, long c, long d, long e, long f, long g, long h) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
		this.h = h;
	}

	public Hash512(MessageDigest digest) throws DigestException {
		byte[] data = SmallBuf.COPY.buffer;
		int off = SmallBuf.COPY.getSection();
		digest.digest(data, off, 32);
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
		this.e = SmallBuf.getLong(data, off + 32);
		this.f = SmallBuf.getLong(data, off + 40);
		this.g = SmallBuf.getLong(data, off + 48);
		this.h = SmallBuf.getLong(data, off + 56);
	}

	public Hash512(Hash256 a, Hash256 b) {
		this.a = a.a;
		this.b = a.b;
		this.c = a.c;
		this.d = a.d;
		this.e = b.a;
		this.f = b.b;
		this.g = b.c;
		this.h = b.d;
	}

	public Hash512(HashCode code) {
		byte[] data = SmallBuf.COPY.buffer;
		int off = SmallBuf.COPY.getSection();
		code.writeBytesTo(data, off, 32);
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
		this.e = SmallBuf.getLong(data, off + 32);
		this.f = SmallBuf.getLong(data, off + 40);
		this.g = SmallBuf.getLong(data, off + 48);
		this.h = SmallBuf.getLong(data, off + 56);
	}

	public Hash512(byte[] data, int off) {
		this.a = SmallBuf.getLong(data, off + 0);
		this.b = SmallBuf.getLong(data, off + 8);
		this.c = SmallBuf.getLong(data, off + 16);
		this.d = SmallBuf.getLong(data, off + 24);
		this.e = SmallBuf.getLong(data, off + 32);
		this.f = SmallBuf.getLong(data, off + 40);
		this.g = SmallBuf.getLong(data, off + 48);
		this.h = SmallBuf.getLong(data, off + 56);
	}

	public Hash512(String base16) {
		int index = 0;
		this.a = Long.parseLong(base16, index += 16, index, 16);
		this.b = Long.parseLong(base16, index += 16, index, 16);
		this.c = Long.parseLong(base16, index += 16, index, 16);
		this.d = Long.parseLong(base16, index += 16, index, 16);
		this.e = Long.parseLong(base16, index += 16, index, 16);
		this.f = Long.parseLong(base16, index += 16, index, 16);
		this.g = Long.parseLong(base16, index += 16, index, 16);
		this.h = Long.parseLong(base16, index += 16, index, 16);
	}

	@Override
	public int compareTo(@NotNull Hash512 o) {
		long comp;
		if((comp = this.a - o.a) != 0) return (int) comp;
		if((comp = this.b - o.b) != 0) return (int) comp;
		if((comp = this.c - o.c) != 0) return (int) comp;
		if((comp = this.d - o.d) != 0) return (int) comp;
		if((comp = this.e - o.e) != 0) return (int) comp;
		if((comp = this.f - o.f) != 0) return (int) comp;
		if((comp = this.g - o.g) != 0) return (int) comp;
		if((comp = this.h - o.h) != 0) return (int) comp;
		return 0;
	}

	@Override
	public byte getByte(int index) {
		int modIndex = 56 - (index & 7) * 8;
		return switch(index >> 6) {
			case 0 -> (byte) (this.a >> modIndex & 0xff);
			case 1 -> (byte) (this.b >> modIndex & 0xff);
			case 2 -> (byte) (this.c >> modIndex & 0xff);
			case 3 -> (byte) (this.d >> modIndex & 0xff);
			case 4 -> (byte) (this.e >> modIndex & 0xff);
			case 5 -> (byte) (this.f >> modIndex & 0xff);
			case 6 -> (byte) (this.g >> modIndex & 0xff);
			case 7 -> (byte) (this.h >> modIndex & 0xff);
			default -> (byte)0;
		};
	}

	@Override
	public int bytes() {
		return 64;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof Hash512 h && this.compareTo(h) == 0;
	}

	@Override
	public int hashCode() {
		int result = Long.hashCode(this.a);
		result = 31 * result + Long.hashCode(this.b);
		result = 31 * result + Long.hashCode(this.c);
		result = 31 * result + Long.hashCode(this.d);
		result = 31 * result + Long.hashCode(this.e);
		result = 31 * result + Long.hashCode(this.f);
		result = 31 * result + Long.hashCode(this.g);
		result = 31 * result + Long.hashCode(this.h);
		return result;
	}
}
