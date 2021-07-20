package net.devtech.oeel.v0.api;

import net.devtech.oeel.v0.api.util.hash.HashKey;

public record EncryptionEntry(HashKey validation, byte[] key) {
}