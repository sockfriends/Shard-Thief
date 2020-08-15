package io.github.haykam821.shardthief.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class ShardThiefMap {
	private final Structure structure;

	public ShardThiefMap(Structure structure) {
		this.structure = structure;
	}
	
	public Structure getStructure() {
		return this.structure;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new ShardThiefChunkGenerator(server, this.structure);
	}
}
