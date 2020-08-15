package io.github.haykam821.shardthief.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class ShardThiefConfig {
	public static final Codec<ShardThiefConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(ShardThiefConfig::getPlayerConfig),
			Codec.INT.optionalFieldOf("starting_counts", 20).forGetter(ShardThiefConfig::getStartingCounts),
			Codec.INT.optionalFieldOf("restart_counts", 5).forGetter(ShardThiefConfig::getRestartCounts)
		).apply(instance, ShardThiefConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final int startingCounts;
	private final int restartCounts;

	public ShardThiefConfig(PlayerConfig playerConfig, int startingCounts, int restartCounts) {
		this.playerConfig = playerConfig;
		this.startingCounts = startingCounts;
		this.restartCounts = restartCounts;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public int getStartingCounts() {
		return this.startingCounts;
	}

	public int getRestartCounts() {
		return this.restartCounts;
	}
}
