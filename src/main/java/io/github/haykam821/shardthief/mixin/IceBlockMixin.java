package io.github.haykam821.shardthief.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.haykam821.shardthief.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(IceBlock.class)
public class IceBlockMixin {
	@Inject(method = "melt", at = @At("HEAD"), cancellable = true)
	private void applyIceMeltingGameRule(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
		if (gameSpace == null) return;

		if (gameSpace.testRule(Main.ICE_MELTING) == RuleResult.DENY) {
			ci.cancel();
		}
	}
}
