package io.github.townyadvanced.townyprovinces.settings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TownyProvincesSettings {
	
	public static boolean isTownyProvincesEnabled() {
		return Settings.getBoolean(ConfigNodes.ENABLED);
	}

	//TODO - Later make this "get worlds" etc.
	public static World getWorld() {
		return Bukkit.getWorld(getWorldName());
	}
	
	public static String getWorldName() {
		return Settings.getString(ConfigNodes.WORLD_NAME);
	}

	public static Location getTopLeftWorldCornerLocation() {
		String locationString = Settings.getString(ConfigNodes.TOP_LEFT_WORLD_CORNER_LOCATION);
		String[] locationArray = locationString.split(",");		
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));	
	}
	
	public static Location getBottomRightWorldCornerLocation() {
		String locationString = Settings.getString(ConfigNodes.BOTTOM_RIGHT_WORLD_CORNER_LOCATION);
		String[] locationArray = locationString.split(",");
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));
	}
	
	public static int getAverageProvinceSizeInSquareMetres() {
		return Settings.getInt(ConfigNodes.AVERAGE_PROVINCE_SIZE_IN_SQUARE_METRES);
	}
	
	public static int getMinAllowedDistanceBetweenProvinceHomeBlocks() {
		return Settings.getInt(ConfigNodes.MIN_ALLOWED_DISTANCE_BETWEEN_PROVINCE_HOMEBLOCKS);
	}
	
	public static double getMaxAllowedVarianceBetweenIdealAndActualNumProvinces() {
		return Settings.getDouble(ConfigNodes.MAX_ALLOWED_VARIANCE_BETWEEN_IDEAL_AND_ACTUAL_NUM_PROVINCES);
	}
	
	public static int getMinAllowedDistanceBetweenRegionHomeBlocks() {
		return 50;
	}
	
	public static int getDefaultNumRegionsPerProvince() {
		return 3;
	}

	public static int getRegionBlockLength() {
		return 16;
	}
}
