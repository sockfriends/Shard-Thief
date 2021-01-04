package io.github.haykam821.shardthief;

import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.phase.ShardThiefWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class Main implements ModInitializer {
	public static final String MOD_ID = "shardthief";

	private static final Identifier SHARD_THIEF_ID = new Identifier(MOD_ID, "shard_thief");
	public static final GameType<ShardThiefConfig> SHARD_THIEF_TYPE = GameType.register(SHARD_THIEF_ID, ShardThiefWaitingPhase::open, ShardThiefConfig.CODEC);

	public static final GameRule ICE_MELTING = new GameRule();

	@Override
	public void onInitialize() {
		return;
	}
}
