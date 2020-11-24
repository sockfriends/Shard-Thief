package io.github.haykam821.shardthief.game.phase;

import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import io.github.haykam821.shardthief.game.map.ShardThiefMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class ShardThiefWaitingPhase {
	private final GameSpace gameSpace;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;

	public ShardThiefWaitingPhase(GameSpace gameSpace, ShardThiefMap map, ShardThiefConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<ShardThiefConfig> context) {
		ShardThiefMapBuilder mapBuilder = new ShardThiefMapBuilder();
		ShardThiefMap map = mapBuilder.create(context.getServer());

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			ShardThiefWaitingPhase waiting = new ShardThiefWaitingPhase(game.getSpace(), map, context.getConfig());

			GameWaitingLobby.applyTo(game, context.getConfig().getPlayerConfig());

			ShardThiefActivePhase.setRules(game, RuleResult.DENY);

			// Listeners
			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
			game.on(RequestStartListener.EVENT, waiting::requestStart);
		});
	}

	private StartResult requestStart() {
		ShardThiefActivePhase.open(this.gameSpace, this.map, this.config);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		ShardThiefActivePhase.spawn(this.gameSpace.getWorld(), this.map, player, this.gameSpace.getPlayerCount() - 1);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player
		ShardThiefActivePhase.spawn(this.gameSpace.getWorld(), this.map, player, 0);
		return ActionResult.SUCCESS;
	}
}
