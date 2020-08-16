package io.github.haykam821.shardthief.game.phase;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.shardthief.game.DroppedShard;
import io.github.haykam821.shardthief.game.PlayerShardEntry;
import io.github.haykam821.shardthief.game.ShardInventoryManager;
import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.ShardThiefCountBar;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.Game;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class ShardThiefActivePhase {
	private final ServerWorld world;
	private final GameWorld gameWorld;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private final Set<PlayerShardEntry> players;
	private final ShardThiefCountBar countBar = new ShardThiefCountBar();

	private PlayerShardEntry shardHolder;
	private int ticksUntilCount;
	private DroppedShard droppedShard;

	public ShardThiefActivePhase(GameWorld gameWorld, ShardThiefMap map, ShardThiefConfig config, Set<ServerPlayerEntity> players) {
		this.world = gameWorld.getWorld();
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;

		this.players = players.stream().map(player -> {
			return new PlayerShardEntry(player, this.config.getStartingCounts());
		}).collect(Collectors.toSet());

		BlockPos size = this.map.getStructure().getSize();
		this.placeShard(new BlockPos(size.getX(), 64, size.getZ()));
	}

	public static void setRules(Game game) {
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.ALLOW);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
	}

	public static void open(GameWorld gameWorld, ShardThiefMap map, ShardThiefConfig config) {
		ShardThiefActivePhase active = new ShardThiefActivePhase(gameWorld, map, config, gameWorld.getPlayers());

		gameWorld.openGame(game -> {
			ShardThiefActivePhase.setRules(game);

			// Listeners
			game.on(GameCloseListener.EVENT, active::close);
			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameTickListener.EVENT, active::tick);
			game.on(PlayerAddListener.EVENT, active::addPlayer);
			game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
			game.on(PlayerRemoveListener.EVENT, active::removePlayer);
		});
	}

	private void open() {
		for (PlayerShardEntry entry : this.players) {
			ServerPlayerEntity player = entry.getPlayer();
			player.setGameMode(GameMode.ADVENTURE);
			ShardInventoryManager.giveNonShardInventory(player);
			ShardThiefActivePhase.spawn(this.world, this.map, player);
		}
	}

	private void close() {
		this.countBar.remove();
	}

	public float getTimerBarPercent() {
		if (this.shardHolder == null) {
			return 1;
		}
		return this.shardHolder.getCounts() / (float) this.config.getStartingCounts();
	}

	private void clearShard() {
		if (this.shardHolder == null) return;

		this.shardHolder.getPlayer().inventory.clear();
		ShardInventoryManager.giveNonShardInventory(this.shardHolder.getPlayer());

		if (this.shardHolder.getCounts() < this.config.getRestartCounts()) {
			this.shardHolder.setCounts(this.config.getRestartCounts());
		}
		this.shardHolder = null;

		this.ticksUntilCount = 35;
	}

	private void setShardHolder(PlayerShardEntry entry) {
		this.clearShard();
		this.shardHolder = entry;

		entry.getPlayer().inventory.clear();
		ShardInventoryManager.giveShardInventory(entry.getPlayer());
	}

	private void sendStealMessage() {
		Text stealText = this.shardHolder.getStealMessage();
		for (PlayerShardEntry entry : this.players) {
			TitleS2CPacket packet = new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, stealText);
			entry.getPlayer().networkHandler.sendPacket(packet);
		}
	}

	private void pickUpShard(PlayerShardEntry entry) {
		this.setShardHolder(entry);

		this.droppedShard.reset(this.world);
		this.droppedShard = null;

		this.world.playSound(null, entry.getPlayer().getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
		this.sendStealMessage();
	}

	private BlockPos findDropPos(BlockPos initialPos) {
		BlockPos.Mutable pos = initialPos.mutableCopy();
		while (true) {
			if (pos.getY() == 0) return pos;

			BlockState state = this.world.getBlockState(pos);
			if (state.isSolidBlock(world, pos) || state.getBlock() instanceof SlabBlock) {
				return pos;
			}

			pos.move(Direction.DOWN);
		}
	}

	private void placeShard(BlockPos pos) {
		this.droppedShard = new DroppedShard(pos, this.world.getBlockState(pos), this.config.getDroppedShardInvulnerability());
		this.droppedShard.place(this.world);
	}

	private void dropShard() {
		BlockPos pos = this.findDropPos(this.shardHolder.getPlayer().getBlockPos());
		this.placeShard(pos);

		this.clearShard();

		this.world.playSound(null, pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 1, 1);
	}

	private Formatting getCountTitleColor() {
		int counts = this.shardHolder.getCounts();
		if (counts <= 1) {
			return Formatting.RED;
		} else if (counts <= 3) {
			return Formatting.GOLD;
		} else {
			return Formatting.YELLOW;
		}
	}

	private void tickCounts() {
		this.shardHolder.decrementCounts();
		if (this.shardHolder.getCounts() <= 0) {
			Text message = this.shardHolder.getWinMessage();
			for (PlayerShardEntry entry : this.players) {
				entry.getPlayer().sendMessage(message, false);
				entry.getPlayer().playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 1);
			}

			this.gameWorld.close();
			return;
		} else if (this.shardHolder.getCounts() <= 5) {
			String countString = Integer.toString(this.shardHolder.getCounts());
			Text countText = new LiteralText(countString).formatted(this.getCountTitleColor()).formatted(Formatting.BOLD);

			for (PlayerShardEntry entry : this.players) {
				TitleS2CPacket packet = new TitleS2CPacket(TitleS2CPacket.Action.TITLE, countText);
				entry.getPlayer().networkHandler.sendPacket(packet);
	
				entry.getPlayer().playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.PLAYERS, 1, 1.5f);
			}
		}

		this.ticksUntilCount = 35;
	}

	private boolean canPlayerPickUpDroppedShard(PlayerEntity player) {
		return this.droppedShard != null && this.droppedShard.canPlayerPickUp(player);
	}

	private void tick() {
		this.countBar.tick(this);

		if (this.droppedShard != null) {
			this.droppedShard.tick();
		}
	
		if (this.shardHolder != null) {
			if (this.ticksUntilCount <= 0) {
				this.tickCounts();
			}
			this.ticksUntilCount -= 1;
		}

		for (PlayerShardEntry entry : this.players) {
			if (entry.equals(this.shardHolder)) continue;

			ServerPlayerEntity player = entry.getPlayer();
			if (this.canPlayerPickUpDroppedShard(player)) {
				this.pickUpShard(entry);
			}
		}
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.setSpectator(player);
		this.countBar.addPlayer(player);
	}

	private void removePlayer(ServerPlayerEntity player) {
		// Drop shard when player is removed
		if (this.shardHolder != null && player.equals(this.shardHolder.getPlayer())) {
			this.dropShard();
		}

		this.players.removeIf(entry -> {
			return player.equals(entry.getPlayer());
		});
		this.countBar.removePlayer(player);
	}

	private void tryTransferShard(ServerPlayerEntity damagedPlayer, DamageSource source) {
		if (!(source.getAttacker() instanceof ServerPlayerEntity)) return;
		ServerPlayerEntity attacker = (ServerPlayerEntity) source.getAttacker();

		if (this.shardHolder == null) return;

		PlayerEntity shardPlayer = this.shardHolder.getPlayer();
		if (!damagedPlayer.equals(shardPlayer)) return;
		if (attacker.equals(shardPlayer)) return;

		for (PlayerShardEntry entry : this.players) {
			if (attacker.equals(entry.getPlayer())) {
				if (source.isProjectile()) {
					this.dropShard();
					if (source.getSource() instanceof ProjectileEntity) {
						source.getSource().kill();
					}
 				} else {
					this.setShardHolder(entry);
					this.sendStealMessage();
				}
				return;
			}
		}
	}

	private boolean onPlayerDamage(ServerPlayerEntity damagedPlayer, DamageSource source, float damage) {
		this.tryTransferShard(damagedPlayer, source);
		return true;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		ShardThiefActivePhase.spawn(this.world, this.map, player);
		return ActionResult.SUCCESS;
	}

	public static void spawn(ServerWorld world, ShardThiefMap map, ServerPlayerEntity player) {
		BlockPos size = map.getStructure().getSize();
		player.teleport(world, size.getX(), 65, size.getZ() - 8, 0, 0);
	}
}
