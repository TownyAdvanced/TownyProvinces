package io.github.townyadvanced.townyprovinces.util;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

public class MoneyUtil {

	/**
	 * Recalculate province prices
	 *
	 * Step 1: Assign provinces to regions
	 * Step 2: Cycle regions
	 * Step 3: For each region, calculate the average cost per province, excluding outliers (anything over x3 the simple average)
	 * Step 4. For each region, Calculate the cost limit per province. This will be averageCostWithoutOutliers x (configured)provinceCostLimitRation
	 * Step 5: Cycle provinces in the region
	 * Step 6: For each province, if the cost is above the limit, limit it to the limit.
	 */
	public static void recalculateProvincePrices() {
		TownyProvinces.info("Recalculating province prices");
		TownyProvincesSettings.recalculateProvincesInRegions();
		double townNewCost;
		double townUpkeepCost;
		double townNewCostLimit;
		double townUpkeepCostLimit;
		int numCoordsInProvince;
		for(Region region: TownyProvincesSettings.getOrderedRegionsList()) {
			//Set standard costs
			for(Province province: region.getProvinces()) {
				numCoordsInProvince = province.getListOfCoordsInProvince().size();
				townNewCost = region.getNewTownCostPerChunk() * numCoordsInProvince;
				townUpkeepCost = region.getUpkeepTownCostPerChunk() * numCoordsInProvince;
				province.setNewTownCost(townNewCost);
				province.setUpkeepTownCost(townUpkeepCost);
			}
			//Limit costs where required
			townNewCostLimit = region.getAverageNewTownCostWithoutOutliers() * TownyProvincesSettings.getProvinceCostLimitProportion();
			townUpkeepCostLimit = region.getAverageUpkeepTownCostWithoutOutliers() * TownyProvincesSettings.getProvinceCostLimitProportion();
			for(Province province: region.getProvinces()) {
				province.setNewTownCost(Math.min(province.getNewTownCost(), townNewCostLimit));
				province.setUpkeepTownCost(Math.min(province.getUpkeepTownCost(), townUpkeepCostLimit));
			}
		}
		//Save data
		DataHandlerUtil.saveAllData();
		TownyProvinces.info("Province Prices Recalculated");
	}
	
}
