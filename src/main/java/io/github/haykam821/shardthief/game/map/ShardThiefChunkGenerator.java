package io.github.haykam821.shardthief.game.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

public final class ShardThiefChunkGenerator extends GameChunkGenerator {
	private final Structure structure;

	public ShardThiefChunkGenerator(RegistryKey<Biome> biome, Structure structure, MinecraftServer server) {
		super(GameChunkGenerator.createBiomeSource(server, biome), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
		this.structure = structure;
	}

	private StructureProcessorRule getReplaceRule(Block input, Block output) {
		return new StructureProcessorRule(
			new BlockMatchRuleTest(input),
			AlwaysTrueRuleTest.INSTANCE,
			output.getDefaultState()
		);
	}


	private List<StructureProcessorRule> getRules(Block terracotta, Block concrete) {
		List<StructureProcessorRule> rules = new ArrayList<>();

		rules.add(this.getReplaceRule(Blocks.WHITE_TERRACOTTA, terracotta));
		rules.add(this.getReplaceRule(Blocks.WHITE_CONCRETE, concrete));
	
		return rules;
	}

	private void placeStructure(ChunkRegion region, BlockPos pos, BlockRotation rotation, Block terracotta, Block concrete) {
		StructurePlacementData placementData = new StructurePlacementData();

		placementData.setRotation(rotation);
		placementData.addProcessor(new RuleStructureProcessor(this.getRules(terracotta, concrete)));

		this.structure.place(region, pos, placementData, region.getRandom());
	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
		if (region.getCenterChunkX() != 0) return;
		if (region.getCenterChunkZ() != 0) return;

		BlockPos pos = new BlockPos(0, 64, 0);

		BlockPos size = this.structure.getSize();
		int x = size.getX() * 2 - 1;
		int z = size.getZ() * 2 - 1;

		this.placeStructure(region, pos, BlockRotation.NONE, Blocks.LIME_TERRACOTTA, Blocks.LIME_CONCRETE);
		this.placeStructure(region, pos.add(x, 0, 0), BlockRotation.CLOCKWISE_90, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.BLUE_CONCRETE);
		this.placeStructure(region, pos.add(x, 0, z), BlockRotation.CLOCKWISE_180, Blocks.RED_TERRACOTTA, Blocks.RED_CONCRETE);
		this.placeStructure(region, pos.add(0, 0, z), BlockRotation.COUNTERCLOCKWISE_90, Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_CONCRETE);
	}
}
