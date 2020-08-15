package io.github.haykam821.shardthief.game.phase;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import io.github.haykam821.shardthief.game.map.ShardThiefMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;

public class ShardThiefWaitingPhase {
	private final GameWorld gameWorld;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;

	public ShardThiefWaitingPhase(GameWorld gameWorld, ShardThiefMap map, ShardThiefConfig config) {
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;
	}

	public static CompletableFuture<Void> open(GameOpenContext<ShardThiefConfig> context) {
		ShardThiefMapBuilder mapBuilder = new ShardThiefMapBuilder();

		return mapBuilder.create(context.getServer()).thenAccept(map -> {
			BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.createGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.ADVENTURE);
			GameWorld gameWorld = context.openWorld(worldConfig);

			ShardThiefWaitingPhase waiting = new ShardThiefWaitingPhase(gameWorld, map, context.getConfig());

			gameWorld.openGame(game -> {
				ShardThiefActivePhase.setRules(game);

				// Listeners
				game.on(PlayerAddListener.EVENT, waiting::addPlayer);
				game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
				game.on(OfferPlayerListener.EVENT, waiting::offerPlayer);
				game.on(RequestStartListener.EVENT, waiting::requestStart);
			});
		});
	}

	private boolean isFull() {
		return this.gameWorld.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	private JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	private StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameWorld.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.notEnoughPlayers();
		}

		ShardThiefActivePhase.open(this.gameWorld, this.map, this.config);
		return StartResult.ok();
	}

	private void addPlayer(ServerPlayerEntity player) {
		ShardThiefActivePhase.spawn(this.gameWorld.getWorld(), this.map, player);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player
		ShardThiefActivePhase.spawn(this.gameWorld.getWorld(), this.map, player);
		return ActionResult.SUCCESS;
	}
}
