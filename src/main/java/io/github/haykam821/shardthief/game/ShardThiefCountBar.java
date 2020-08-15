package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class ShardThiefCountBar {
	private final ServerBossBar bar = new ServerBossBar(new LiteralText("Shard Thief"), BossBar.Color.BLUE, BossBar.Style.PROGRESS);

	public void tick(ShardThiefActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setPercent(percent);
	}

	public void remove() {
		this.bar.clearPlayers();
		this.bar.setVisible(false);
	}

	public void addPlayer(ServerPlayerEntity player) {
		this.bar.addPlayer(player);
	}

	public void removePlayer(ServerPlayerEntity player) {
		this.bar.removePlayer(player);
	}
}