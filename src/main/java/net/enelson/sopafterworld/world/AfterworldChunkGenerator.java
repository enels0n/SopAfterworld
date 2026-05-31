package net.enelson.sopafterworld.world;

import java.util.Random;

import net.enelson.sopafterworld.SopAfterworld;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public class AfterworldChunkGenerator extends ChunkGenerator {

	private long cachedSeed = Long.MIN_VALUE;
	private SimplexNoiseGenerator primaryNoise;
	private SimplexNoiseGenerator detailNoise;
	private SimplexNoiseGenerator ridgeNoise;
	private SimplexNoiseGenerator patchNoise;
	private SimplexNoiseGenerator basinNoise;
	private SimplexNoiseGenerator microBasinNoise;
	private SimplexNoiseGenerator waveNoise;
	private SimplexNoiseGenerator macroWaveNoise;

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
		refreshNoise(world.getSeed());

		ChunkData data = createChunkData(world);
		int minY = 0;
		int maxY = 255;
		int lavaLevel = clamp(SopAfterworld.generatorLavaLevel, minY + 4, maxY - 8);

		for (int localX = 0; localX < 16; localX++) {
			for (int localZ = 0; localZ < 16; localZ++) {
				int worldX = chunkX * 16 + localX;
				int worldZ = chunkZ * 16 + localZ;
				biome.setBiome(localX, localZ, Biome.NETHER_WASTES);

				int terrainHeight = calculateHeight(worldX, worldZ, lavaLevel, maxY);
				Material topMaterial = selectTopMaterial(worldX, worldZ, terrainHeight, lavaLevel);

				for (int y = minY; y <= terrainHeight; y++) {
					if (y <= minY + random.nextInt(Math.max(1, SopAfterworld.generatorBedrockLayers))) {
						data.setBlock(localX, y, localZ, Material.BEDROCK);
						continue;
					}

					if (y >= terrainHeight - 2) {
						data.setBlock(localX, y, localZ, topMaterial);
					} else {
						data.setBlock(localX, y, localZ, selectFillMaterial(worldX, y, worldZ));
					}
				}

				if (terrainHeight < lavaLevel) {
					for (int y = terrainHeight + 1; y <= lavaLevel; y++) {
						data.setBlock(localX, y, localZ, Material.LAVA);
					}
				}
			}
		}

		return data;
	}

	@Override
	public boolean shouldGenerateCaves() {
		return false;
	}

	@Override
	public boolean shouldGenerateDecorations() {
		return false;
	}

	@Override
	public boolean shouldGenerateMobs() {
		return true;
	}

	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}

	private void refreshNoise(long seed) {
		if (seed == cachedSeed && primaryNoise != null) {
			return;
		}

		cachedSeed = seed;
		primaryNoise = new SimplexNoiseGenerator(seed ^ 0x1A2B3C4DL);
		detailNoise = new SimplexNoiseGenerator(seed ^ 0x55667788L);
		ridgeNoise = new SimplexNoiseGenerator(seed ^ 0xCAFEBABEL);
		patchNoise = new SimplexNoiseGenerator(seed ^ 0x10203040L);
		basinNoise = new SimplexNoiseGenerator(seed ^ 0x0F0E0D0CL);
		microBasinNoise = new SimplexNoiseGenerator(seed ^ 0x1234ABCDL);
		waveNoise = new SimplexNoiseGenerator(seed ^ 0x55AA10F0L);
		macroWaveNoise = new SimplexNoiseGenerator(seed ^ 0x2468ACE0L);
	}

	private int calculateHeight(int worldX, int worldZ, int lavaLevel, int maxY) {
		double hills = primaryNoise.noise(worldX * SopAfterworld.generatorPrimaryScale,
				worldZ * SopAfterworld.generatorPrimaryScale) * (SopAfterworld.generatorHeightVariation * 0.72D);
		double detail = detailNoise.noise(worldX * SopAfterworld.generatorDetailScale,
				worldZ * SopAfterworld.generatorDetailScale) * (SopAfterworld.generatorDetailHeight * 1.1D);
		double ridges = Math.abs(ridgeNoise.noise(worldX * SopAfterworld.generatorRidgeScale,
				worldZ * SopAfterworld.generatorRidgeScale)) * 1.7D;
		double roundedRidge = ridgeNoise.noise(worldX * (SopAfterworld.generatorRidgeScale * 0.45D),
				worldZ * (SopAfterworld.generatorRidgeScale * 0.45D)) * 1.0D;
		double waves = waveNoise.noise(worldX * (SopAfterworld.generatorPrimaryScale * 1.7D),
				worldZ * (SopAfterworld.generatorPrimaryScale * 1.7D)) * 2.6D;
		double macroWaves = macroWaveNoise.noise(worldX * (SopAfterworld.generatorPrimaryScale * 0.78D),
				worldZ * (SopAfterworld.generatorPrimaryScale * 0.78D)) * 1.1D;

		double baseLand = lavaLevel + 0.4D + hills + detail + ridges + roundedRidge + waves + macroWaves;

		double basin = basinNoise.noise(worldX * (SopAfterworld.generatorPrimaryScale * 0.58D),
				worldZ * (SopAfterworld.generatorPrimaryScale * 0.58D));
		double microBasin = microBasinNoise.noise(worldX * (SopAfterworld.generatorDetailScale * 0.72D),
				worldZ * (SopAfterworld.generatorDetailScale * 0.72D));

		double basinDepth = 0.0D;
		if (basin < -0.50D) {
			double normalized = (-0.50D - basin) / 0.50D;
			basinDepth += normalized * normalized * 3.8D;
		}
		if (microBasin < -0.55D) {
			double normalized = (-0.55D - microBasin) / 0.45D;
			basinDepth += normalized * 2.0D;
		}

		double height = baseLand - basinDepth;
		return clamp((int) Math.round(height), lavaLevel - 2, Math.min(maxY - 6, lavaLevel + 7));
	}

	private Material selectTopMaterial(int worldX, int worldZ, int terrainHeight, int lavaLevel) {
		double patch = patchNoise.noise(worldX * 0.04D, worldZ * 0.04D);
		if (terrainHeight <= lavaLevel + 2) {
			if (terrainHeight >= lavaLevel && patch > 0.18D && patch < 0.46D) {
				return Material.MAGMA_BLOCK;
			}
			return Material.BLACKSTONE;
		}
		if (patch > 0.45D) {
			return Material.SOUL_SAND;
		}
		if (patch < -0.45D) {
			return Material.GRAVEL;
		}
		if (terrainHeight >= SopAfterworld.generatorBaseHeight + SopAfterworld.generatorHeightVariation / 2) {
			return Material.BLACKSTONE;
		}
		return Material.NETHERRACK;
	}

	private Material selectFillMaterial(int worldX, int y, int worldZ) {
		double patch = patchNoise.noise(worldX * 0.035D, y * 0.02D, worldZ * 0.035D);
		if (patch > 0.62D) {
			return Material.BLACKSTONE;
		}
		if (patch < -0.60D) {
			return Material.SOUL_SOIL;
		}
		return Material.NETHERRACK;
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
