package io.github.haykam821.shardthief.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public class ShardThiefMapConfig {
	public static final Codec<ShardThiefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("structure_id").forGetter(ShardThiefMapConfig::getStructureId),
			Identifier.CODEC.fieldOf("biome_id").forGetter(ShardThiefMapConfig::getBiomeId),
			Codec.INT.optionalFieldOf("spawn_y_offset", 1).forGetter(ShardThiefMapConfig::getSpawnYOffset)
		).apply(instance, ShardThiefMapConfig::new);
	});

	private final Identifier structureId;
	private final Identifier biomeId;
	private final int spawnYOffset;

	public ShardThiefMapConfig(Identifier structureId, Identifier biomeId, int spawnYOffset) {
		this.structureId = structureId;
		this.biomeId = biomeId;
		this.spawnYOffset = spawnYOffset;
	}

	public Identifier getStructureId() {
		return this.structureId;
	}

	public Identifier getBiomeId() {
		return this.biomeId;
	}

	public int getSpawnYOffset() {
		return this.spawnYOffset;
	}
}