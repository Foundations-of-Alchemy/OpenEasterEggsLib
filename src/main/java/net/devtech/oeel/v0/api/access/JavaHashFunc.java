package net.devtech.oeel.v0.api.access;

import java.util.function.Function;

import com.google.gson.JsonElement;

public interface JavaHashFunc<T> extends Function<JsonElement, HashFunction<T>> {}
