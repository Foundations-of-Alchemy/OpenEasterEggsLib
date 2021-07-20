package net.devtech.oeel.v0.api.util.hash;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * a rotating thread-safe buffer
 */
public final class SmallBuf {
	static final SmallBuf INSTANCE = new SmallBuf();
	public final byte[] buffer = new byte[4096];
	public final AtomicInteger reserve = new AtomicInteger(-64);

	public int getSection() {
		return this.reserve.addAndGet(64) & 4095;
	}

	public static long getLong(byte[] buf, int off) {
		long current = 0;
		for(int i = 0; i < 8; i++) {
			current |= (buf[i + off] & 0xFFL) << (56 - i * 8);
		}
		return current;
	}

	public static void writeLong(long l, byte[] buf, int off) {
		for(int i = 0; i < 8; i++) {
			buf[off + i] = (byte) (l >>> (56 - i * 8));
		}
	}
}
