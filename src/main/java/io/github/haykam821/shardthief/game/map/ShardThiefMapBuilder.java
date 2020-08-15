package io.github.haykam821.shardthief.game.map;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.shardthief.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ShardThiefMapBuilder {
	private static final Identifier STRUCTURE_ID = new Identifier(Main.MOD_ID, "block_fort");

	public CompletableFuture<ShardThiefMap> create(MinecraftServer server) {
		return CompletableFuture.supplyAsync(() -> {
			Structure structure = server.getStructureManager().getStructureOrBlank(STRUCTURE_ID);
			return new ShardThiefMap(structure);
		}, Util.getMainWorkerExecutor());
	}
}
