package net.devtech.oeel.v0.api.data;

import java.util.Collection;

import com.google.common.hash.HashCode;
import net.devtech.oeel.v0.api.OEEL;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

public interface RecipeManager<T> {
	/**
	 * get the recipe for the given input.
	 * Remember, because {@link OEEL#RECIPES} are global, you must add something to your info to make it unique.
	 * A simple way is to add a string, such as the identifier.
	 *
	 * eg. info input item a + info input item b + info input item c + "mymod:uu_crafting" = output
	 *
	 * this will prevent it the info from colliding with other recipes.
	 */
	@Nullable
	T getForInput(HashCode input, Identifier itemHashFunction, Identifier blockHashFunction, Identifier entityHashFunction);
}
