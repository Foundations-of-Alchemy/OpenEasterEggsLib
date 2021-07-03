package net.devtech.oeel.v0.api;

import com.google.common.hash.HashCode;
import net.devtech.oeel.v0.api.util.BiHasher;

public record EncryptionEntry(HashCode validation, HashCode encryption) {
	public EncryptionEntry(BiHasher hasher) {
		this(hasher.hashA(), hasher.hashB());
	}
}