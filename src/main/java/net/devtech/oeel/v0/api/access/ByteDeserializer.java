package net.devtech.oeel.v0.api.access;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.hash.HashKey;

public interface ByteDeserializer<T> {
	/**
	 * must be packable by {@link IdentifierPacker}.
	 * The easiest way to ensure uniqueness is to put your mod id in the string and some unique-ifier. Eg. "oeel/tex".
	 *
	 * should be less than 10 characters
	 *
	 * @see IdentifierPacker#pack(String)
	 */
	String magic();

	T newInstance();

	void read(T instance, DataInputStream buffer, HashKey inputHash) throws IOException;
}
