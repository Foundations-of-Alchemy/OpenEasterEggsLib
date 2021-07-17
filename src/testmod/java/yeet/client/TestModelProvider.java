package yeet.client;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import yeet.A;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;

public class TestModelProvider implements ModelResourceProvider {
	public static final Map<Identifier, AbstractBakedModel> MODELS = new HashMap<>();

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
		return MODELS.get(resourceId);
	}

	public static void add(AbstractBakedModel model, String...names) {
		for (String name : names) {
			MODELS.put(A.id(name), model);
		}
	}
}