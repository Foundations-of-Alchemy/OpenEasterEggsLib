package net.devtech.oeel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.entity.BeaconBlockEntity;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccess {
	@Accessor
	int getLevel();
}
