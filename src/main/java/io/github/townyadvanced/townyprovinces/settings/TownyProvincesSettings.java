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
	private final static List<String> orderedRegionNames = new ArrayList<>();  //Name in the order of files
	
	public static boolean loadRegionDefinitions() {
		regionDefinitions.clear();
		orderedRegionNames.clear();
		List<File> regionDefinitionFiles = FileUtil.readRegionDefinitionFiles();
		Collections.sort(regionDefinitionFiles);
		Map<String, String> regionDefinitions;
		String regionName;
		for(File regionDefinitionFile: regionDefinitionFiles) {
			regionDefinitions = FileMgmt.loadFileIntoHashMap(regionDefinitionFile);
			regionName = regionDefinitions.get("region_name");
			TownyProvincesSettings.regionDefinitions.put(regionName, regionDefinitions);
			orderedRegionNames.add(regionName);
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
	public static @Nullable World getWorld() {
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
	
	public static int getAverageProvinceSize(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("average_province_size");
		return Integer.parseInt(numString);
	}
	
	public static int getMinimumStartDistanceBetweenBrushes(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("minimum_start_distance_between_brushes");
		return Integer.parseInt(numString);
	}
	
	public static double getMaxAllowedVarianceBetweenIdealAndActualNumProvinces(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_allowed_variance_between_ideal_and_actual_num_provinces");
		return Double.parseDouble(numString);
	}
	
	public static int getChunkSideLength() {
		return 16; //Same as a chunk. Best not to change
	}

	public static int getMaxBrushMoves(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_brush_moves");
		return Integer.parseInt(numString);
	}

	public static int getBrushSquareRadiusInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("brush_square_radius_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getMinBrushMoveAmountInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("min_brush_move_amount_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getMaxBrushMoveAmountInChunks(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_brush_move_amount_in_chunks");
		return Integer.parseInt(numString);
	}

	public static int getMaxAreaClaimedPerBrush(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_area_claimed_per_brush");
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
		return Integer.parseInt(Settings.getString(ConfigNodes.LAND_PROVINCE_BORDER_COLOUR),16);
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
	
	public static List<String> getOrderedRegionNames() {
		return orderedRegionNames;
	}
	
	public static String getNameOfFirstRegion() {
		return orderedRegionNames.get(0);
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
		int homeBlockRealX = province.getHomeBlock().getX() * getChunkSideLength();
		int homeBlockRealZ = province.getHomeBlock().getZ() * getChunkSideLength();
		
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
