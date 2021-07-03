package net.devtech.oeel.v0.api.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

import net.minecraft.util.Identifier;

public record BiHasher(Hasher a, Hasher b) implements Hasher {
	public static BiHasher createDefault(boolean testingForEmpty) {
		BiHasher hasher = new BiHasher(OEELHashing.FUNCTION.newHasher(), testingForEmpty ? null : OEELHashing.FUNCTION.newHasher());
		if(!testingForEmpty) {
			hasher.b().putLong(OEELEncrypting.MAGIC);
		}
		return hasher;
	}

	public Hasher putIdentifier(Identifier id) {
		this.putString(id.getNamespace(), StandardCharsets.US_ASCII);
		this.putString(id.getPath(), StandardCharsets.US_ASCII);
		return this;
	}

	@Override
	public Hasher putByte(byte b) {
		if(this.a != null) this.a.putByte(b);
		if(this.b != null) this.b.putByte(b);
		return this;
	}

	@Override
	public Hasher putBytes(byte[] bytes) {
		if(this.a != null) this.a.putBytes(bytes);
		if(this.b != null) this.b.putBytes(bytes);
		return this;
	}

	@Override
	public Hasher putBytes(byte[] bytes, int off, int len) {
		if(this.a != null) this.a.putBytes(bytes, off, len);
		if(this.b != null) this.b.putBytes(bytes, off, len);
		return this;
	}

	@Override
	public Hasher putShort(short s) {
		if(this.a != null) this.a.putShort(s);
		if(this.b != null) this.b.putShort(s);
		return this;
	}

	@Override
	public Hasher putInt(int i) {
		if(this.a != null) this.a.putInt(i);
		if(this.b != null) this.b.putInt(i);
		return this;
	}

	@Override
	public Hasher putLong(long l) {
		if(this.a != null) this.a.putLong(l);
		if(this.b != null) this.b.putLong(l);
		return this;
	}

	@Override
	public Hasher putFloat(float f) {
		if(this.a != null) this.a.putFloat(f);
		if(this.b != null) this.b.putFloat(f);
		return this;
	}

	@Override
	public Hasher putDouble(double d) {
		if(this.a != null) this.a.putDouble(d);
		if(this.b != null) this.b.putDouble(d);
		return this;
	}

	@Override
	public Hasher putBoolean(boolean b) {
		if(this.a != null) this.a.putBoolean(b);
		if(this.b != null) this.b.putBoolean(b);
		return this;
	}

	@Override
	public Hasher putChar(char c) {
		if(this.a != null) this.a.putChar(c);
		if(this.b != null) this.b.putChar(c);
		return this;
	}

	@Override
	public Hasher putUnencodedChars(CharSequence charSequence) {
		if(this.a != null) this.a.putUnencodedChars(charSequence);
		if(this.b != null) this.b.putUnencodedChars(charSequence);
		return this;
	}

	@Override
	public Hasher putString(CharSequence charSequence, Charset charset) {
		if(this.a != null) this.a.putString(charSequence, charset);
		if(this.b != null) this.b.putString(charSequence, charset);
		return this;
	}

	@Override
	public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
		if(this.a != null) this.a.putObject(instance, funnel);
		if(this.b != null) this.b.putObject(instance, funnel);
		return this;
	}

	@Override
	public HashCode hash() {
		throw new UnsupportedOperationException();
	}

	public HashCode hashA() {
		return this.a == null ? null : a.hash();
	}

	public HashCode hashB() {
		return this.b == null ? null : b.hash();
	}
}
