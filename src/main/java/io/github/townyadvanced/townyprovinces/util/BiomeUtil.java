package io.github.townyadvanced.townyprovinces.util;

import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

public class BiomeUtil {

	public static boolean isProvinceMainlyOceanBiome(Province province) {
		List<TPCoord> coordsInProvince = province.getListOfCoordsInProvince();
		Biome biome;
		TPCoord coordToTest;
		for(int i = 0; i < 10; i++) {
			coordToTest = coordsInProvince.get((int)(Math.random() * coordsInProvince.size()));
			String worldName = TownyProvincesSettings.getWorldName();
			World world = Bukkit.getWorld(worldName);
			biome = lookupBiome(coordToTest, world);
			try {
				Thread.sleep(200); //Sleep because the biome lookup can be hard on processor
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if(!biome.name().toLowerCase().contains("ocean") && !biome.name().toLowerCase().contains("beach")) {
				return false;
			}
		}
		return true;
	}
	
	public static Biome lookupBiome(TPCoord coordToTest, World world) {
		int x = (coordToTest.getX() * TownyProvincesSettings.getChunkSideLength()) + 8;
		int z = (coordToTest.getZ() * TownyProvincesSettings.getChunkSideLength()) + 8;
		if(TownyProvincesSettings.isBiomeLookupByBlock()) {
			return world.getHighestBlockAt(x,z).getBiome();
		} else {
			return world.getBiome(x,64, z);
		}
	}
}
