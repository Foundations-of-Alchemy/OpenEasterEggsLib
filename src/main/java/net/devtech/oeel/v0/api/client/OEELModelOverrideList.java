package net.devtech.oeel.v0.api.client;

import io.github.astrarre.itemview.v0.fabric.ItemKey;
import net.devtech.oeel.v0.api.OEEL;
import net.devtech.oeel.v0.api.access.HashFunction;
import net.devtech.oeel.v0.api.data.HashFunctionManager;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class OEELModelOverrideList extends ModelOverrideList {
	@Nullable
	@Override
	public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		for(HashFunction<ItemKey> value : HashFunctionManager.ITEM_COMP.getMap().values()) {

		}
		return super.apply(model, stack, world, entity, seed);
	}
}
