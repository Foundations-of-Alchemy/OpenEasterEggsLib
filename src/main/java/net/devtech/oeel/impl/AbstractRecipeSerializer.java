package net.devtech.oeel.impl;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public interface AbstractRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
	@Override
	T read(Identifier id, JsonObject json);

	void write(JsonObject object, T recipe);

	@Override
	default T read(Identifier id, PacketByteBuf buf) {
		JsonObject object = NbtOps.INSTANCE.convertTo(JsonOps.COMPRESSED, buf.readNbt()).getAsJsonObject();
		return this.read(id, object);
	}

	@Override
	default void write(PacketByteBuf buf, T recipe) {
		JsonObject write = new JsonObject();
		this.write(write, recipe);
		NbtElement element = JsonOps.COMPRESSED.convertTo(NbtOps.INSTANCE, write);
		buf.writeNbt((NbtCompound) element);
	}
}
