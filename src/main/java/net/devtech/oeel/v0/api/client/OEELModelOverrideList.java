package net.devtech.oeel.v0.api.client;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Iterables;
import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.impl.OEELImpl;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import net.devtech.oeel.v0.api.data.ObfResourceManager;
import net.devtech.oeel.v0.api.util.EncryptionEntry;
import net.devtech.oeel.v0.api.util.hash.BiHasher;
import net.devtech.oeel.v0.api.util.hash.HashKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class OEELModelOverrideList extends ModelOverrideList {
	public static final Identifier ID = OEELImpl.id("model");
	final Map<HashKey, BakedModel> cache = new HashMap<>();
	final OEELBakedModel model;

	public OEELModelOverrideList(OEELBakedModel model) {
		this.model = model;
	}

	@Nullable
	@Override
	public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		for(var mapEntry : HashFunctionManager.CLIENT.itemComp.getMap().entrySet()) {
			HashFunction<ItemKey> value = mapEntry.getValue();
			EncryptionEntry entry;
			try(BiHasher hasher = BiHasher.createDefault(true)) {
				hasher.putIdentifier(ID);
				value.hash(hasher, ItemKey.of(stack));
				entry = hasher.hash();
			}

			BakedModel baked = this.cache.computeIfAbsent(entry.entryKey(), key -> {
				var models = ObfResourceManager.client.decryptOnce(entry, (buffer, inputHash) -> {
					if(OEELImpl.validate(buffer, mapEntry.getKey())) {
						return JsonUnbakedModel.deserialize(buffer.readUTF());
					} else {
						return null;
					}
				});

				JsonUnbakedModel unbaked = Iterables.getOnlyElement(models);
				return unbaked.bake(this.model.loader, this.model.textureGetter, this.model.rotationContainer, this.model.modelId);
			});

			if(baked == null) {
				return null;
			} else {
				return baked.getOverrides().apply(model, stack, world, entity, seed);
			}
		}
		return super.apply(model, stack, world, entity, seed);
	}
}
