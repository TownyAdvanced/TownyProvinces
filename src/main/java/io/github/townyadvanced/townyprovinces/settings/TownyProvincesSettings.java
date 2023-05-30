package io.github.townyadvanced.townyprovinces.settings;

import com.palmergames.util.FileMgmt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.Map;

public class TownyProvincesSettings {
	
	private static Map<String,String> provinceGenerationInstructions = null;

	/**
	 * Set this before generating a province
	 * 
	 * @param provinceGenerationFile set of instructions
	 */
	public static void setProvinceGenerationInstructions(File provinceGenerationFile) {
		provinceGenerationInstructions = FileMgmt.loadFileIntoHashMap(provinceGenerationFile);
	}
	
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

	public static Location getTopLeftCornerLocation() {
		String locationString = provinceGenerationInstructions.get("top_left_corner_location");
		String[] locationArray = locationString.split(",");		
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));	
	}
	
	public static Location getBottomRightWorldCornerLocation() {
		String locationString = provinceGenerationInstructions.get("bottom_right_corner_location");
		String[] locationArray = locationString.split(",");
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));
	}
	
	public static int getAverageProvinceSizeInSquareMetres() {
		String numString = provinceGenerationInstructions.get("average_province_size_in_square_metres");
		return Integer.parseInt(numString);
	}
	
	public static int getMinAllowedDistanceBetweenProvinceHomeBlocks() {
		String numString = provinceGenerationInstructions.get("min_allowed_distance_between_province_home_blocks");
		return Integer.parseInt(numString);
	}
	
	public static double getMaxAllowedVarianceBetweenIdealAndActualNumProvinces() {
		String numString = provinceGenerationInstructions.get("max_allowed_variance_between_ideal_and_actual_num_provinces");
		return Double.parseDouble(numString);
	}
	
	public static int getProvinceBlockSideLength() {
		return 16; //Same as a chunk. Best not to change
	}

	public static boolean isDeleteProvincesContainingJustOcean() {
		String booleanString = provinceGenerationInstructions.get("delete_ocean_provinces");
		return Boolean.parseBoolean(booleanString);
	}

	public static int getNumberOfProvincePaintingCycles() {
		String numString = provinceGenerationInstructions.get("number_of_province_painting_cycles");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushSquareRadiusInChunks() {
		String numString = provinceGenerationInstructions.get("province_creator_brush_square_radius_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushMinMoveInChunks() {
		String numString = provinceGenerationInstructions.get("province_creator_brush_min_move_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushMaxMoveInChunks() {
		String numString = provinceGenerationInstructions.get("province_creator_brush_max_move_in_chunks");
		return Integer.parseInt(numString);
	}
	
	public static int getBorderColour() {
		return Settings.getInt(ConfigNodes.BORDER_COLOUR);
	}
}
