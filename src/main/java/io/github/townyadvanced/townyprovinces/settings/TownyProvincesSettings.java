package io.github.townyadvanced.townyprovinces.settings;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TownyProvincesSettings {

	private static final Map<String, Map<String, String>> regionDefinitions = new HashMap<>();
	private final static List<String> orderedRegionNames = new ArrayList<>();  //Name in the order of files

	public static boolean loadRegionDefinitions() {
		regionDefinitions.clear();
		orderedRegionNames.clear();
		List<File> regionDefinitionFiles = FileUtil.readRegionDefinitionFiles();
		Collections.sort(regionDefinitionFiles);
		Map<String, String> regionDefinitions;
		String regionName;
		for (File regionDefinitionFile : regionDefinitionFiles) {
			regionDefinitions = FileMgmt.loadFileIntoHashMap(regionDefinitionFile);
			regionName = regionDefinitions.get("region_name");
			TownyProvincesSettings.regionDefinitions.put(regionName, regionDefinitions);
			orderedRegionNames.add(regionName);
		}
		//Ensure none of them are titled "ALL"
		for (String name : TownyProvincesSettings.getRegionDefinitions().keySet()) {
			if (name.equalsIgnoreCase("all")) {
				TownyProvinces.severe("Error: One region was named 'All'. This is not allowed");
				return false;
			}
		}
		return true;
	}

	public static Map<String, String> getRegionDefinitions(String regionName) {
		return regionDefinitions.get(regionName);
	}

	public static Map<String, Map<String, String>> getRegionDefinitions() {
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
		Map<String, String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String locationString = regionDefinitions.get("top_left_corner_location");
		String[] locationArray = locationString.split(",");
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()), 0, Integer.parseInt(locationArray[1].trim()));
	}

	public static Location getBottomRightCornerLocation(String regionName) {
		Map<String, String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String locationString = regionDefinitions.get("bottom_right_corner_location");
		String[] locationArray = locationString.split(",");
		return new Location(getWorld(), Integer.parseInt(locationArray[0].trim()), 0, Integer.parseInt(locationArray[1].trim()));
	}

	public static int getAverageProvinceSize(String regionName) {
		Map<String, String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("average_province_size");
		return Integer.parseInt(numString);
	}
	
	public static int getChunkSideLength() {
		return 16;
	}

	public static int getMaxBrushMoves(String regionName) {
		Map<String, String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString = regionDefinitions.get("max_brush_moves");
		return Integer.parseInt(numString);
	}

	public static int getBrushSquareRadiusInChunks(String regionName) {
		double provinceSquareRadius = calculateProvinceSquareRadius(regionName);
		double brushSquareRadiusPercent = getBrushSquareRadiusAsPercentageOfProvinceSquareRadius(regionName);
		double brushSquareRadius = provinceSquareRadius / 100 * brushSquareRadiusPercent;
		int brushSquareRadiusInChunks = (int)((brushSquareRadius / getChunkSideLength()) + 0.5);
		brushSquareRadiusInChunks = Math.max(brushSquareRadiusInChunks, 1);
		return brushSquareRadiusInChunks;
	}
	
	public static int getMaxBrushMoveInChunks(String regionName) {
		double brushSquareRadius = getBrushSquareRadiusInChunks(regionName) * getChunkSideLength();
		double brushMaxMovePercent = getBrushMaxMoveAsPercentageOfBrushSquareRadius(regionName);
		double brushMaxMove = brushSquareRadius / 100 * brushMaxMovePercent;
		int brushMaxMoveInChunks = (int)((brushMaxMove / getChunkSideLength()) + 0.5);
		brushMaxMoveInChunks = Math.max(brushMaxMoveInChunks, 1);
		return brushMaxMoveInChunks;
	}

	public static int getMinBrushMoveInChunks(String regionName) {
		double brushMaxMove = getMaxBrushMoveInChunks(regionName) * getChunkSideLength();
		double brushMinMovePercent = getBrushMinMoveAsPercentageOfBrushMaxMove(regionName);
		double brushMinMove = brushMaxMove / 100 * brushMinMovePercent;
		int brushMinMoveInChunks = (int)((brushMinMove / getChunkSideLength()) + 0.5);
		brushMinMoveInChunks = Math.max(brushMinMoveInChunks, 1);
		return brushMinMoveInChunks;
	}
	
	private static int getBrushSquareRadiusAsPercentageOfProvinceSquareRadius(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString =  regionDefinitions.get("brush_square_radius_as_percentage_of_province_square_radius");
		return Integer.parseInt(numString);
	}

	private static int getBrushMaxMoveAsPercentageOfBrushSquareRadius(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString =  regionDefinitions.get("brush_max_move_as_percentage_of_brush_square_radius");
		return Integer.parseInt(numString);
	}

	private static int getBrushMinMoveAsPercentageOfBrushMaxMove(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numString =  regionDefinitions.get("brush_min_move_as_percentage_of_brush_max_move");
		return Integer.parseInt(numString);
	}

	public static int calculateProvinceSquareRadius(String regionName) {
		double averageProvinceSize = getAverageProvinceSize(regionName);
		return (int)((Math.sqrt(averageProvinceSize)) / 2);
	}

	public static double getNewTownCostPerChunk(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numberString = regionDefinitions.get("new_town_cost_per_chunk");
		return Double.parseDouble(numberString);
	}

	public static double getUpkeepTownCostPerChunk(String regionName) {
		Map<String,String> regionDefinitions = TownyProvincesSettings.getRegionDefinitions(regionName);
		String numberString = regionDefinitions.get("upkeep_town_cost_per_chunk");
		return Double.parseDouble(numberString);
	}
	
	public static int getLandProvinceBorderWeight() {
		return Settings.getInt(ConfigNodes.LAND_PROVINCE_BORDER_WEIGHT);
	}

	public static double getLandProvinceBorderOpacity() {
		return Settings.getDouble(ConfigNodes.LAND_PROVINCE_BORDER_OPACITY);
	}

	public static int getPauseMillisecondsBetweenBiomeLookups() {
		return Settings.getInt(ConfigNodes.PAUSE_MILLISECONDS_BETWEEN_BIOME_LOOKUPS);
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

	public static boolean isRoadsEnabled() {
		return Settings.getBoolean(ConfigNodes.ROADS_ENABLED);
	}

	public static boolean isPortsEnabled() {
		return Settings.getBoolean(ConfigNodes.PORTS_ENABLED);
	}

	public static double getPortsPurchasePrice() {
		return Settings.getDouble(ConfigNodes.PORTS_PURCHASE_PRICE);
	}

	public static double getPortsUpkeepCost() {
		return Settings.getDouble(ConfigNodes.PORTS_UPKEEP_COST);
	}
	public static boolean isJumpNodesEnabled() {
		return Settings.getBoolean(ConfigNodes.JUMP_NODES_ENABLED);
	}

	public static double getJumpNodesPurchasePrice() {
		return Settings.getDouble(ConfigNodes.JUMP_NODES_PURCHASE_PRICE);
	}

	public static double getJumpNodesUpkeepCost() {
		return Settings.getDouble(ConfigNodes.JUMP_NODES_UPKEEP_COST);
	}
	
	public static double getPortsMaxFastTravelRange() { return Settings.getDouble(ConfigNodes.PORTS_MAX_FAST_TRAVEL_RANGE); }

	public static boolean isBiomeCostAdjustmentsEnabled() {
		return Settings.getBoolean(ConfigNodes.BIOME_COST_ADJUSTMENTS_ENABLED);
	}
	public static double getBiomeCostAdjustmentsWater() { return Settings.getDouble(ConfigNodes.BIOME_COST_ADJUSTMENTS_WATER); }

	public static double getBiomeCostAdjustmentsHotLand() { return Settings.getDouble(ConfigNodes.BIOME_COST_ADJUSTMENTS_HOT_LAND); }
	public static double getBiomeCostAdjustmentsColdLand() { return Settings.getDouble(ConfigNodes.BIOME_COST_ADJUSTMENTS_COLD_LAND); }
	
	public static @Nullable BufferedImage getTownCostsIcon() {
		URL imageURL;
		try {
			imageURL = new URL(Settings.getString(ConfigNodes.TOWN_COSTS_ICON_URL));
			return ImageIO.read(imageURL);
		} catch (MalformedURLException e) {
			TownyProvinces.severe("Error: Invalid Town Costs Icon URL in configuration file.");
			return null;
		} catch (IOException e) {
			TownyProvinces.severe("Error: Failed to load Town Costs Icon from URL provided in configuration file.");
			return null;
		}
	}
	
	public static int getTownCostsIconWidth() { return Settings.getInt(ConfigNodes.TOWN_COSTS_ICON_WIDTH); }
	
	public static int getTownCostsIconHeight() { return Settings.getInt(ConfigNodes.TOWN_COSTS_ICON_HEIGHT); }

	public static boolean getDynmapUsesTownCostsIcon() { return Settings.getBoolean(ConfigNodes.DYNMAP_USE_ICON); }

	public static int getProvincesLayerPriority() { return Settings.getInt(ConfigNodes.PROVINCES_LAYER_PRIORITY); }

	public static int getProvincesLayerZIndex() { return Settings.getInt(ConfigNodes.PROVINCES_LAYER_ZINDEX); }

	public static boolean getProvincesLayerIsToggleable() { return Settings.getBoolean(ConfigNodes.PROVINCES_LAYER_TOGGLE); }

	public static int getTownCostsLayerPriority() { return Settings.getInt(ConfigNodes.TOWN_COSTS_LAYER_PRIORITY); }

	public static int getTownCostsLayerZIndex() { return Settings.getInt(ConfigNodes.TOWN_COSTS_LAYER_ZINDEX); }

	public static boolean getTownCostsLayerIsToggleable() { return Settings.getBoolean(ConfigNodes.TOWN_COSTS_LAYER_TOGGLE); }

}
