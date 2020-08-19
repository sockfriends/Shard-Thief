package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.widget.BossBarWidget;

public class ShardThiefCountBar {
	private final BossBarWidget bar;

	public ShardThiefCountBar(GameWorld gameWorld) {
		this.bar = BossBarWidget.open(gameWorld.getPlayerSet(), new LiteralText("Shard Thief"), BossBar.Color.BLUE, BossBar.Style.PROGRESS);
	}

	public void tick(ShardThiefActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setProgress(percent);
	}

	public void remove() {
		this.bar.close();
	}
}