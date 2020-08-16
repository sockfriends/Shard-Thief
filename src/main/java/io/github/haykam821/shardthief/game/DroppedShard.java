package io.github.haykam821.shardthief.game;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class DroppedShard implements Tickable {
	private static final BlockState FULL_DROP_STATE = Blocks.PRISMARINE.getDefaultState();
	private static final BlockState SLAB_DROP_STATE = Blocks.PRISMARINE_SLAB.getDefaultState();

	private final BlockPos pos;
	private final BlockState oldState;
	private int invulnerability;

	public DroppedShard(BlockPos pos, BlockState oldState, int invulnerability) {
		this.pos = pos;
		this.oldState = oldState;
		this.invulnerability = invulnerability;
	}

	private BlockState getBlockState() {
		if (this.oldState.getBlock() instanceof SlabBlock) {
			return SLAB_DROP_STATE
				.with(Properties.SLAB_TYPE, this.oldState.get(Properties.SLAB_TYPE))
				.with(Properties.WATERLOGGED, this.oldState.get(Properties.WATERLOGGED));
		}
		return FULL_DROP_STATE;
	}

	public void place(WorldAccess world) {
		world.setBlockState(this.pos, this.getBlockState(), 3);
	}

	public void reset(WorldAccess world) {
		world.setBlockState(this.pos, this.oldState, 3);
	}

	public boolean canPlayerPickUp(PlayerEntity player) {
		return this.invulnerability <= 0 && this.pos.equals(player.getLandingPos());
	}

	@Override
	public void tick() {
		if (this.invulnerability > 0) {
			this.invulnerability -= 1;
		}
	}
}