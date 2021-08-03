package net.devtech.oeel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ItemModelGenerator;

@Mixin(ModelLoader.class)
public interface ModelLoaderAccess {
	@Accessor
	static ItemModelGenerator getITEM_MODEL_GENERATOR() { throw new UnsupportedOperationException(); }
}
