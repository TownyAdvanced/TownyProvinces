package io.github.townyadvanced.townyprovinces.util;

import io.github.townyadvanced.townyprovinces.jobs.land_validation.BiomeType;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class BiomeUtil {
	
	public static Biome lookupBiome(TPCoord coordToTest, World world) {
		int x = (coordToTest.getX() * TownyProvincesSettings.getChunkSideLength()) + 8;
		int z = (coordToTest.getZ() * TownyProvincesSettings.getChunkSideLength()) + 8;
		return world.getHighestBlockAt(x,z).getBiome();
	}

	public static BiomeType getBiomeType(World world, TPCoord coordToTest) {
		int x = (coordToTest.getX() * TownyProvincesSettings.getChunkSideLength()) + 8;
		int z = (coordToTest.getZ() * TownyProvincesSettings.getChunkSideLength()) + 8;
		Material material = world.getHighestBlockAt(x,z).getType();
		try {
			Thread.sleep(TownyProvincesSettings.getPauseMillisecondsBetweenBiomeLookups()); //Sleep because the biome lookup can be hard on processor
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if(material == Material.WATER) {
			return BiomeType.WATER;
		} else if (material == Material.SAND || material == Material.RED_SAND) {
			return BiomeType.HOT_LAND;
		} else if (material == Material.SNOW || material == Material.SNOW_BLOCK || material == Material.ICE || material == Material.BLUE_ICE) {
			return BiomeType.COLD_LAND;
		} else {
			return BiomeType.GOOD_LAND;
		}
	}
}
