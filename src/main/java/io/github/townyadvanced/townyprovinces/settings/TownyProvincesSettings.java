package io.github.townyadvanced.townyprovinces.settings;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TownyProvincesSettings {
	
	private static final Map<String, Map<String,String>> regionDefinitions = new HashMap<>();

	public static boolean loadRegionDefinitions() {
		List<File> regionDefinitionFiles = FileUtil.readRegionDefinitionFiles();
		Map<String, String> regionDefinitions;
		for(File regionDefinitionFile: regionDefinitionFiles) {
			regionDefinitions = FileMgmt.loadFileIntoHashMap(regionDefinitionFile);
			TownyProvincesSettings.regionDefinitions.put(regionDefinitions.get("region_name"), regionDefinitions);
		}
		//Ensure none of them are titled "ALL"
		for(String name: TownyProvincesSettings.getRegionDefinitions().keySet()) {
			if(name.equalsIgnoreCase("all")) {
				TownyProvinces.severe("Error: One region was named 'All'. This is not allowed");
				return false;
			}
		}
		return true;
	}

	public static Map<String,String> getRegionDefinitions(String regionName) {
		return regionDefinitions.get(regionName);
	}
	
	public static Map<String, Map<String,String>> getRegionDefinitions() {
		return regionDefinitions;
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

	public static Location getTopLeftCornerLocation(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String locationString = regionDefinitions.get("top_left_corner_location");
		String[] locationArray = locationString.split(",");		
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));	
	}
	
	public static Location getBottomRightCornerLocation(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String locationString = regionDefinitions.get("bottom_right_corner_location");
		String[] locationArray = locationString.split(",");
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()),0,Integer.parseInt(locationArray[1].trim()));
	}
	
	public static int getProvinceSizeEstimateForPopulatingInSquareMetres(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("province_size_estimate_for_populating_in_square_metres");
		return Integer.parseInt(numString);
	}
	
	public static int getMinAllowedDistanceBetweenProvinceHomeBlocks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("min_allowed_distance_between_province_home_blocks");
		return Integer.parseInt(numString);
	}
	
	public static double getMaxAllowedVarianceBetweenIdealAndActualNumProvinces(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_allowed_variance_between_ideal_and_actual_num_provinces");
		return Double.parseDouble(numString);
	}
	
	public static int getProvinceBlockSideLength() {
		return 16; //Same as a chunk. Best not to change
	}

	public static int getNumberOfProvincePaintingCycles(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("number_of_province_painting_cycles");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushSquareRadiusInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("province_creator_brush_square_radius_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushMinMoveInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("province_creator_brush_min_move_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushMaxMoveInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("province_creator_brush_max_move_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getProvinceCreatorBrushClaimLimitInSquareMetres(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("province_creator_brush_claim_limit_in_square_metres");
		return Integer.parseInt(numString);
	}
	
	public static int getNewTownCost(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String intString = regionDefinitions.get("new_town_cost");
		return Integer.parseInt(intString);
	}

	public static int getUpkeepTownCost(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String intString = regionDefinitions.get("upkeep_town_cost");
		return Integer.parseInt(intString);
	}
	
	public static int getLandProvinceBorderWeight() {
		return Settings.getInt(ConfigNodes.LAND_PROVINCE_BORDER_WEIGHT);
	}

	public static double getLandProvinceBorderOpacity() {
		return Settings.getDouble(ConfigNodes.LAND_PROVINCE_BORDER_OPACITY);
	}

	public static int getLandProvinceBorderColour() {
		return Settings.getInt(ConfigNodes.LAND_PROVINCE_BORDER_COLOUR);
	}

	public static int getSeaProvinceBorderWeight() {
		return Settings.getInt(ConfigNodes.SEA_PROVINCE_BORDER_WEIGHT);
	}

	public static double getSeaProvinceBorderOpacity() {
		return Settings.getDouble(ConfigNodes.SEA_PROVINCE_BORDER_OPACITY);
	}

	public static int getSeaProvinceBorderColour() {
		return Integer.parseInt(Settings.getString(ConfigNodes.SEA_PROVINCE_BORDER_COLOUR),16);
	}
	
	public static String getNameOfFirstRegion() {
		List<String> regionNames = new ArrayList<>(regionDefinitions.keySet());
		Collections.sort(regionNames);
		return regionNames.get(0);
	}
	
	public static @Nullable String getCaseSensitiveRegionName(String givenRegionName) {
		for(String regionName: regionDefinitions.keySet()) {
			if(regionName.equalsIgnoreCase(givenRegionName)) {
				return regionName;
			}
		}
		return null;
	}

	public static boolean isProvinceInRegion(Province province, String regionName) {
		Location topLeftCornerLocation = getTopLeftCornerLocation(regionName);
		Location bottomRightCornerLocation = getBottomRightCornerLocation(regionName);
		int homeBlockRealX = province.getHomeBlock().getX() * getProvinceBlockSideLength();
		int homeBlockRealZ = province.getHomeBlock().getZ() * getProvinceBlockSideLength();
		
		if(homeBlockRealX  > topLeftCornerLocation.getBlockX()
				&& homeBlockRealZ > topLeftCornerLocation.getBlockZ()
				&& homeBlockRealX < bottomRightCornerLocation.getBlockX()
				&& homeBlockRealZ < bottomRightCornerLocation.getBlockZ()) {
			return true;
		} else {
			return false;
		}
	}
}
