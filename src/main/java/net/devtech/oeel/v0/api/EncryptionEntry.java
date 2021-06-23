package net.devtech.oeel.v0.api;

import com.google.common.hash.HashCode;

public record EncryptionEntry(HashCode validation, HashCode encryption) {}