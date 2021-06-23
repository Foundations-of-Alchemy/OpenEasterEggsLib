package net.devtech.oeel.v0.api.data;

import java.util.Collection;

import com.google.common.hash.HashCode;
import net.devtech.oeel.v0.api.OEEL;
import org.jetbrains.annotations.Nullable;

public interface RecipeManager<T> {
	/**
	 * get the recipe for the given input.
	 * Remember, because {@link OEEL#BASE_RECIPES} & {@link OEEL#ITEM_RECIPES} are global, you must add something to your hash to make it unique.
	 * A simple way is to add a string, such as the identifier.
	 *
	 * eg. hash input item a + hash input item b + hash input item c + "mymod:uu_crafting" = output
	 *
	 * this will prevent it the hash from colliding with other recipes.
	 */
	@Nullable
	T getForInput(HashCode input);
	
	Collection<T> getAll();

	class Ref<T> implements RecipeManager<T> {
		RecipeManager<T> manager;

		@Override
		public Collection<T> getAll() {
			return this.manager.getAll();
		}

		@Override
		public @Nullable T getForInput(HashCode input) {
			return this.manager.getForInput(input);
		}
	}
}
