package net.devtech.oeel.v0.api.util;

import java.nio.charset.Charset;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

public class VersionedHasher implements Hasher {
	public final Hasher hasher;
	protected int version;

	public VersionedHasher(Hasher hasher) {
		this.hasher = hasher;
	}

	public int getVersion() {
		this.version++;
		return this.version;
	}

	@Override
	public Hasher putByte(byte b) {
		this.version++;
		return this.hasher.putByte(b);
	}

	@Override
	public Hasher putBytes(byte[] bytes) {
		this.version++;
		return this.hasher.putBytes(bytes);
	}

	@Override
	public Hasher putBytes(byte[] bytes, int off, int len) {
		this.version++;
		return this.hasher.putBytes(bytes, off, len);
	}

	@Override
	public Hasher putShort(short s) {
		this.version++;
		return this.hasher.putShort(s);
	}

	@Override
	public Hasher putInt(int i) {
		this.version++;
		return this.hasher.putInt(i);
	}

	@Override
	public Hasher putLong(long l) {
		this.version++;
		return this.hasher.putLong(l);
	}

	@Override
	public Hasher putFloat(float f) {
		this.version++;
		return this.hasher.putFloat(f);
	}

	@Override
	public Hasher putDouble(double d) {
		this.version++;
		return this.hasher.putDouble(d);
	}

	@Override
	public Hasher putBoolean(boolean b) {
		this.version++;
		return this.hasher.putBoolean(b);
	}

	@Override
	public Hasher putChar(char c) {
		this.version++;
		return this.hasher.putChar(c);
	}

	@Override
	public Hasher putUnencodedChars(CharSequence charSequence) {
		this.version++;
		return this.hasher.putUnencodedChars(charSequence);
	}

	@Override
	public Hasher putString(CharSequence charSequence, Charset charset) {
		this.version++;
		return this.hasher.putString(charSequence, charset);
	}

	@Override
	public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
		this.version++;
		return this.hasher.putObject(instance, funnel);
	}

	@Override
	public HashCode hash() {
		return this.hasher.hash();
	}
}
