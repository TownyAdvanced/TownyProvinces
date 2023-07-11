package io.github.townyadvanced.townyprovinces.objects;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import java.util.List;
import java.util.Set;

public class Province {
	
	private final TPCoord homeBlock;
	private double newTownCost;  //The base cost, not adjusted by biome
	private double upkeepTownCost;  //The base cost, not adjusted by biome
	private ProvinceType type;  //Civilized, Sea, Wasteland
	private final String id; //convenience variable. In memory only. Used for dynmap and file operations
	private double fillOpacity; //Convenience Var, in memory only
	private int fillColour; //Convenience Var, in memory only
	private boolean landValidationRequested;
	private double estimatedProportionOfGoodLand;
	private double estimatedProportionOfWater;
	private double estimatedProportionOfHotLand;
	private double estimatedProportionOfColdLand;
	
	public boolean equals(Object object) {
		if(!(object instanceof Province))
			return false;
		return homeBlock.equals(((Province)object).getHomeBlock());
	}
	
	public Province(TPCoord homeBlock) {
		this.homeBlock = homeBlock;
		this.type = ProvinceType.CIVILIZED;
		this.newTownCost = 0;
		this.upkeepTownCost = 0;
		this.id = "province_x" + homeBlock.getX() + "_z_" + homeBlock.getZ();
		this.fillOpacity = 0;
		this.fillColour = 0;
		this.landValidationRequested = false;
		this.estimatedProportionOfGoodLand = 1;
		this.estimatedProportionOfWater = 0;
		this.estimatedProportionOfHotLand = 0;
		this.estimatedProportionOfColdLand = 0;
	}

	public String getId() {
		return id;
	}
	
	public TPCoord getHomeBlock() {
		return homeBlock;
	}
	
	public void setNewTownCost(double d) {
		this.newTownCost = d;
	}

	public void setUpkeepTownCost(double d) {
		this.upkeepTownCost = d;
	}

	public double getNewTownCost() { return newTownCost; }
	
	public double getBiomeAdjustedNewTownCost() {
		double goodLandCost = newTownCost * estimatedProportionOfGoodLand;
		double waterCost = newTownCost * estimatedProportionOfWater  * TownyProvincesSettings.getBiomeCostAdjustmentsWater();
		double hotLandCost = newTownCost * estimatedProportionOfHotLand * TownyProvincesSettings.getBiomeCostAdjustmentsHotLand();
		double coldLandCost = newTownCost * estimatedProportionOfColdLand *  TownyProvincesSettings.getBiomeCostAdjustmentsColdLand();
		return goodLandCost + waterCost + hotLandCost + coldLandCost;
	}

	public double getUpkeepTownCost() {
		return upkeepTownCost;
	}

	public double getBiomeAdjustedUpkeepTownCost() {
		double goodLandCost = upkeepTownCost * estimatedProportionOfGoodLand;
		double waterCost = upkeepTownCost * estimatedProportionOfWater  * TownyProvincesSettings.getBiomeCostAdjustmentsWater();
		double hotLandCost = upkeepTownCost * estimatedProportionOfHotLand * TownyProvincesSettings.getBiomeCostAdjustmentsHotLand();
		double coldLandCost = upkeepTownCost * estimatedProportionOfColdLand *  TownyProvincesSettings.getBiomeCostAdjustmentsColdLand();
		return goodLandCost + waterCost + hotLandCost + coldLandCost;
	}
	
	public ProvinceType getType() { 
		return type; 
	}
	
	public void setType(ProvinceType p) {
		this.type = p;
	}

	public List<TPCoord> getListOfCoordsInProvince() {
		return TownyProvincesDataHolder.getInstance().getListOfCoordsInProvince(this);
	}

	public void saveData() {
		DataHandlerUtil.saveProvince(this);
	}

	public boolean isLandValidationRequested() {
		return landValidationRequested;
	}

	public void setLandValidationRequested(boolean landValidationRequested) {
		this.landValidationRequested = landValidationRequested;
	}

	public Set<TPCoord> getAdjacentBorderCoords(TPCoord targetCoord) {
		return TownyProvincesDataHolder.getInstance().findAdjacentBorderCoords(targetCoord);
	}

	public double getEstimatedProportionOfGoodLand() {
		return estimatedProportionOfGoodLand;
	}

	public void setEstimatedProportionOfGoodLand(double estimatedProportionOfGoodLand) {
		this.estimatedProportionOfGoodLand = estimatedProportionOfGoodLand;
	}

	public double getEstimatedProportionOfWater() {
		return estimatedProportionOfWater;
	}

	public void setEstimatedProportionOfWater(double estimatedProportionOfWater) {
		this.estimatedProportionOfWater = estimatedProportionOfWater;
	}

	public double getEstimatedProportionOfHotLand() {
		return estimatedProportionOfHotLand;
	}

	public void setEstimatedProportionOfHotLand(double estimatedProportionOfHotLand) {
		this.estimatedProportionOfHotLand = estimatedProportionOfHotLand;
	}

	public double getEstimatedProportionOfColdLand() {
		return estimatedProportionOfColdLand;
	}

	public void setEstimatedProportionOfColdLand(double estimatedProportionOfColdLand) {
		this.estimatedProportionOfColdLand = estimatedProportionOfColdLand;
	}

	public Town getTownOrNull() {
		for(Town town: TownyAPI.getInstance().getTowns()) {
			if(town.hasHomeBlock()) {
				Province province = TownyProvincesDataHolder.getInstance().getProvinceAtWorldCoord(town.getHomeBlockOrNull().getWorldCoord());
				if(province == this) {
					return town;
				}
			}
		}
		return null;
	}

	public double getFillOpacity() {
		return fillOpacity;
	}

	public void setFillOpacity(double fillOpacity) {
		this.fillOpacity = fillOpacity;
	}

	public int getFillColour() {
		return fillColour;
	}

	public void setFillColour(int fillColour) {
		this.fillColour = fillColour;
	}
}
 