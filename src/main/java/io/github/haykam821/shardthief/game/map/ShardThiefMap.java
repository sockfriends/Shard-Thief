package io.github.haykam821.shardthief.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class ShardThiefMap {
	private final ShardThiefMapConfig mapConfig;
	private final Structure structure;
	private final BlockPos centerSpawnPos;

	public ShardThiefMap(ShardThiefMapConfig mapConfig, Structure structure) {
		this.mapConfig = mapConfig;
		this.structure = structure;

		BlockPos size = this.structure.getSize();
		this.centerSpawnPos = new BlockPos(size.getX(), 64 + this.mapConfig.getSpawnYOffset(), size.getZ());
	}

	public ShardThiefMap(ShardThiefMapConfig mapConfig, MinecraftServer server) {
		this(mapConfig, server.getStructureManager().getStructureOrBlank(mapConfig.getStructureId()));
	}

	public ShardThiefMapConfig getMapConfig() {
		return this.mapConfig;
	}
	
	public Structure getStructure() {
		return this.structure;
	}

	public BlockPos getCenterSpawnPos() {
		return this.centerSpawnPos;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new ShardThiefChunkGenerator(RegistryKey.of(Registry.BIOME_KEY, this.mapConfig.getBiomeId()), this.structure, server);
	}
}
