package net.devtech.oeel.v0.api.util.hash;

import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import io.github.astrarre.util.v0.api.Validate;
import org.apache.commons.io.output.NullOutputStream;

public class SHA256Hasher extends AbstractHasher implements AutoCloseable {
	static final OutputStream NULL = NullOutputStream.NULL_OUTPUT_STREAM;
	static final AtomicInteger INDEX = new AtomicInteger();
	static final AtomicReferenceArray<SHA256Hasher> POOL = new AtomicReferenceArray<>(256);

	static final MessageDigest SHA_256;

	static {
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			try {
				sha256.clone();
			} catch(CloneNotSupportedException e) {
				sha256 = null;
			}
			SHA_256 = sha256;
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	final MessageDigest digest;
	final int index;
	OutputStream stream;

	protected SHA256Hasher(int index) {
		this.index = index;
		try {
			if(SHA_256 == null) {
				this.digest = MessageDigest.getInstance("SHA-256");
			} else {
				this.digest = (MessageDigest) SHA_256.clone();
			}
		} catch(Exception e) {
			throw Validate.rethrow(e);
		}
	}

	public static SHA256Hasher getPooled() {
		int index = INDEX.incrementAndGet() & 255;
		SHA256Hasher hasher = POOL.getAndSet(index, null);
		if(hasher == null) {
			return new SHA256Hasher(index);
		} else {
			return hasher;
		}
	}

	@Override
	protected void putByte0(byte b) {
		this.digest.update(b);
	}

	@Override
	protected void putBytes0(byte[] bytes, int off, int len) {
		this.digest.update(bytes, off, len);
	}

	@Override
	public OutputStream asOutputStream() {
		OutputStream stream = this.stream;
		if(stream == null) {
			this.stream = stream = new DigestOutputStream(NULL, this.digest);
		}
		return stream;
	}

	/**
	 * resets this instance once called, so it can be re-used
	 */
	public HashKey hashCompact() {
		return new HashKey(this.digest);
	}

	public byte[] hash() {
		return this.digest.digest();
	}

	/**
	 * returns the object to the pool
	 */
	@Override
	public void close() {
		POOL.set(this.index, this);
	}
}
