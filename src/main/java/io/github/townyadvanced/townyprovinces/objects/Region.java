package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import java.io.File;
import java.util.Map;

public class Region {
	
	private final String name;
	private final int regionMinX;
	private final int regionMaxX;
	private final int regionMinZ;
	private final int regionMaxZ;
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

	public Region(File regionDefinitionFile) {
		Map<String, String> regionDefinitions = FileMgmt.loadFileIntoHashMap(regionDefinitionFile);
		this.name = TownyProvincesSettings.getRegionName(regionDefinitions);
		this.brushSquareRadiusInChunks = TownyProvincesSettings.getBrushSquareRadiusInChunks(regionDefinitions);
		this.provinceSquareRadius = TownyProvincesSettings.calculateProvinceSquareRadius(regionDefinitions);
		this.minBrushMoveAmountInChunks = TownyProvincesSettings.getMinBrushMoveInChunks(regionDefinitions);
		this.maxBrushMoveAmountInChunks = TownyProvincesSettings.getMaxBrushMoveInChunks(regionDefinitions);
		this.claimAreaLimitInSquareMetres = (int)((double)TownyProvincesSettings.getAverageProvinceSize(regionDefinitions) * 1.1);
		this.regionMinX = TownyProvincesSettings.getTopLeftCornerLocation(regionDefinitions).getBlockX();
		this.regionMaxX = TownyProvincesSettings.getBottomRightCornerLocation(regionDefinitions).getBlockX();
		this.regionMinZ = TownyProvincesSettings.getTopLeftCornerLocation(regionDefinitions).getBlockZ();
		this.regionMaxZ = TownyProvincesSettings.getBottomRightCornerLocation(regionDefinitions).getBlockZ();
		this.topLeftRegionCorner = TownyProvincesSettings.getTopLeftCornerLocation(regionDefinitions);
		this.bottomRightRegionCorner = TownyProvincesSettings.getBottomRightCornerLocation(regionDefinitions);
		this.averageProvinceSize = TownyProvincesSettings.getAverageProvinceSize(regionDefinitions);
		this.maxBrushMoves = TownyProvincesSettings.getMaxBrushMoves(regionDefinitions);
		this.protectedLocations = TownyProvincesSettings.getProtectedLocations(regionDefinitions);
		this.newTownCostPerChunk = TownyProvincesSettings.getNewTownCostPerChunk(regionDefinitions);
		this.upkeepTownCostPerChunk = TownyProvincesSettings.getUpkeepTownCostPerChunk(regionDefinitions);
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

}
