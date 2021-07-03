package net.devtech.oeel.v0.api.access;

import java.util.function.Function;

import com.google.gson.JsonElement;

public interface DynamicHashSubstitution<T> extends Function<JsonElement, HashSubstitution<T>> {}
