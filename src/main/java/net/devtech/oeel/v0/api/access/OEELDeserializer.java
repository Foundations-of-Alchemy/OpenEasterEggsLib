package net.devtech.oeel.v0.api.access;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.oeel.v0.api.util.IdentifierPacker;
import net.devtech.oeel.v0.api.util.hash.HashKey;

public interface OEELDeserializer<T> {
	T read(DataInputStream buffer, HashKey inputHash) throws IOException;
}
