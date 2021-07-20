package net.devtech.oeel.v0.api.util;

import net.devtech.oeel.v0.api.util.hash.HashKey;

public record EncryptionEntry(HashKey entryKey, byte[] encryptionKey) {
}