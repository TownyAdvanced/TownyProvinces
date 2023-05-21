package io.github.townyadvanced.townyprovinces.settings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TownyProvincesSettings {
	
	public static boolean isTownyProvincesEnabled() {
		return true;
	}

	//TODO - Later make this "get worlds" etc.
	public static World getWorld() {
		return Bukkit.getWorld("world");
	}

	public static Location getTopLeftWorldCornerLocation() {
		return new Location(getWorld(), -2000,0,-2000);	
	}
	
	public static Location getBottomRightWorldCornerLocation() {
		return new Location(getWorld(), 2000, 0, 2000);
	}
	
	public static int getAverageProvinceSizeInSquareUnits() {
		return 250000;
	}
	
	public static int getMinAllowedDistanceBetweenProvinceHomeBlocks() {
		return 200;
	}
	
	public static double getMaxAllowedVarianceBetweenIdealAndActualNumProvinces() {
		return 0.1;
	}
	
	public static int getMinAllowedDistanceBetweenRegionHomeBlocks() {
		return 50;
	}
	
	public static int getDefaultNumRegionsPerProvince() {
		return 3;
	}
	
}
