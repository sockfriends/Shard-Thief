package io.github.haykam821.shardthief.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerShardEntry implements Comparable<PlayerShardEntry> {
	private final ServerPlayerEntity player;
	private int counts;

	public PlayerShardEntry(ServerPlayerEntity player, int counts) {
		this.player = player;
		this.counts = counts;
	}

	public PlayerShardEntry(ServerPlayerEntity player) {
		this(player, 20);
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public int getCounts() {
		return this.counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public void decrementCounts() {
		this.counts -= 1;
	}

	public Text getWinMessage() {
		return this.getPlayer().getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
	}

	@Override
	public int compareTo(PlayerShardEntry other) {
		return other.counts - this.counts;
	}

	@Override
	public String toString() {
		return "PlayerShardEntry{player=" + this.getPlayer() + ", counts=" + this.getCounts() + "}";
	}
}