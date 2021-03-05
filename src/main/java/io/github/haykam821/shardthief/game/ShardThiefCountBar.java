package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.widget.BossBarWidget;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

public class ShardThiefCountBar {
	private final BossBarWidget bar;

	public ShardThiefCountBar(Text title, GlobalWidgets widgets) {
		this.bar = widgets.addBossBar(title, BossBar.Color.BLUE, BossBar.Style.PROGRESS);
	}

	public void tick(ShardThiefActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setProgress(percent);
	}

	public void remove() {
		this.bar.close();
	}
}
