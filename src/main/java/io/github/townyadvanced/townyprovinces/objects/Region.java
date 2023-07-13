package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Region {
	
	private final String name;
	private final int regionMinX;
	private final int regionMaxX;
	private final int regionMinZ;
	private final int regionMaxZ;
	private final int regionMinXCoord;
	private final int regionMaxXCoord;
	private final int regionMinZCoord;
	private final int regionMaxZCoord;
	private final int minBrushMoveAmountInChunks;
	private final int maxBrushMoveAmountInChunks;
	private final int brushSquareRadiusInChunks;
	private final int provinceSquareRadius;
	private final int claimAreaLimitInSquareMetres;
	private final Location topLeftRegionCorner;
	private final Location bottomRightRegionCorner;
	private final int averageProvinceSize;
	private final int maxBrushMoves;
	private final Map<String, Location> protectedLocations;
	private final double newTownCostPerChunk;
	private final double upkeepTownCostPerChunk;
	private Set<Province> provinces; //Set of provinces. Assigned when required
	
	public Region(File regionDefinitionFile) {
		Map<String, String> regionDefinitions = FileMgmt.loadFileIntoHashMap(regionDefinitionFile);
		this.name = TownyProvincesSettings.getRegionName(regionDefinitions);
		this.brushSquareRadiusInChunks = TownyProvincesSettings.getBrushSquareRadiusInChunks(regionDefinitions);
		this.provinceSquareRadius = TownyProvincesSettings.calculateProvinceSquareRadius(regionDefinitions);
		this.minBrushMoveAmountInChunks = TownyProvincesSettings.getMinBrushMoveInChunks(regionDefinitions);
		this.maxBrushMoveAmountInChunks = TownyProvincesSettings.getMaxBrushMoveInChunks(regionDefinitions);
		this.claimAreaLimitInSquareMetres = (int)((double)TownyProvincesSettings.getAverageProvinceSize(regionDefinitions) * 1.1);
		this.topLeftRegionCorner = TownyProvincesSettings.getTopLeftCornerLocation(regionDefinitions);
		this.bottomRightRegionCorner = TownyProvincesSettings.getBottomRightCornerLocation(regionDefinitions);
		this.regionMinX = this.topLeftRegionCorner.getBlockX();
		this.regionMaxX = this.bottomRightRegionCorner.getBlockX();
		this.regionMinZ = this.topLeftRegionCorner.getBlockZ();
		this.regionMaxZ = this.bottomRightRegionCorner.getBlockZ();
		Coord topLeftCoord = Coord.parseCoord(topLeftRegionCorner);
		Coord bottomRightCoord = Coord.parseCoord(bottomRightRegionCorner);
		this.regionMinXCoord = topLeftCoord.getX();
		this.regionMaxXCoord = bottomRightCoord.getX();
		this.regionMinZCoord = topLeftCoord.getZ();
		this.regionMaxZCoord = bottomRightCoord.getZ();
		this.averageProvinceSize = TownyProvincesSettings.getAverageProvinceSize(regionDefinitions);
		this.maxBrushMoves = TownyProvincesSettings.getMaxBrushMoves(regionDefinitions);
		this.protectedLocations = TownyProvincesSettings.getProtectedLocations(regionDefinitions);
		this.newTownCostPerChunk = TownyProvincesSettings.getNewTownCostPerChunk(regionDefinitions);
		this.upkeepTownCostPerChunk = TownyProvincesSettings.getUpkeepTownCostPerChunk(regionDefinitions);
		this.provinces = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public int getRegionMinX() {
		return regionMinX;
	}

	public int getRegionMaxX() {
		return regionMaxX;
	}

	public int getRegionMinZ() {
		return regionMinZ;
	}

	public int getRegionMaxZ() {
		return regionMaxZ;
	}

	public int getMinBrushMoveAmountInChunks() {
		return minBrushMoveAmountInChunks;
	}

	public int getMaxBrushMoveAmountInChunks() {
		return maxBrushMoveAmountInChunks;
	}

	public int getBrushSquareRadiusInChunks() {
		return brushSquareRadiusInChunks;
	}

	public int getProvinceSquareRadius() {
		return provinceSquareRadius;
	}

	public int getClaimAreaLimitInSquareMetres() {
		return claimAreaLimitInSquareMetres;
	}

	public Location getTopLeftRegionCorner() {
		return topLeftRegionCorner;
	}

	public Location getBottomRightRegionCorner() {
		return bottomRightRegionCorner;
	}

	public int getAverageProvinceSize() {
		return averageProvinceSize;
	}

	public int getMaxBrushMoves() {
		return maxBrushMoves;
	}

	public Map<String, Location> getProtectedLocations() {
		return protectedLocations;
	}

	public double getNewTownCostPerChunk() {
		return newTownCostPerChunk;
	}

	public double getUpkeepTownCostPerChunk() {
		return upkeepTownCostPerChunk;
	}

	public boolean containsCoord(TPCoord coord) {
		if (coord.getX() < regionMinXCoord)
			return false;
		else if (coord.getX() > regionMaxXCoord)
			return false;
		else if (coord.getZ() < regionMinZCoord)
			return false;
		else if (coord.getZ() > regionMaxZCoord)
			return false;
		else 
			return true;
	}

	public int getRegionMinXCoord() {
		return regionMinXCoord;
	}

	public int getRegionMaxXCoord() {
		return regionMaxXCoord;
	}

	public int getRegionMinZCoord() {
		return regionMinZCoord;
	}

	public int getRegionMaxZCoord() {
		return regionMaxZCoord;
	}

	public Set<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(Set<Province> provinces) {
		this.provinces = provinces;
	}

	public void addProvince(Province province) {
		this.provinces.add(province);
	}
	
	public void clearProvinces() {
		provinces.clear();
	}

	public double getAverageNewTownCostWithoutOutliers() {
		//Get average cost
		double averageCost = getAverageNewTownCost(provinces);
		
		//Remove outliers above limit
		double outlierThreshold = averageCost * 3;
		Set<Province> provincesWithoutOutliers = new HashSet<>();
		for(Province province: provinces) {
			if(province.getNewTownCost() < outlierThreshold) {
				provincesWithoutOutliers.add(province);
			}
		}
		
		//Get required average cost
		return getAverageNewTownCost(provincesWithoutOutliers);
	}
	
	public double getAverageNewTownCost(Set<Province> provinces) {
		double result = 0;
		for(Province province: provinces) {
			result += province.getNewTownCost();
		}
		return result / provinces.size();
	}

	public double getAverageUpkeepTownCostWithoutOutliers() {
		//Get average cost
		double averageCost = getAverageUpkeepTownCost(provinces);

		//Remove outliers above limit
		double outlierThreshold = averageCost * 3;
		Set<Province> provincesWithoutOutliers = new HashSet<>();
		for(Province province: provinces) {
			if(province.getUpkeepTownCost() < outlierThreshold) {
				provincesWithoutOutliers.add(province);
			}
		}

		//Get required average cost
		return getAverageUpkeepTownCost(provincesWithoutOutliers);
	}

	public double getAverageUpkeepTownCost(Set<Province> provinces) {
		double result = 0;
		for(Province province: provinces) {
			result += province.getUpkeepTownCost();
		}
		return result / provinces.size();
	}
}
