package io.github.townyadvanced.townyprovinces.util;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
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
		double provinceCostLimitRatio = TownyProvincesSettings.getProvinceCostLimitProportion();  
		TownyProvincesSettings.recalculateProvincesInRegions();
		double townNewCostLimit;
		double townUpkeepCostLimit;
		for(Region region: TownyProvincesSettings.getOrderedRegionsList()) {
			townNewCostLimit = region.getAverageProvinceNewTownCostWithoutOutliers() * provinceCostLimitRatio;
			townUpkeepCostLimit = region.getAverageProvinceUpkeepTownCostWithoutOutliers() * provinceCostLimitRatio;
			for(Province province: region.getProvinces()) {
				if(province.getNewTownCost() > townNewCostLimit) {
					province.setNewTownCost(townNewCostLimit);
				}
				if(province.getNewTownCost() > townUpkeepCostLimit) {
					province.setUpkeepTownCost(townUpkeepCostLimit);
				}
			}
		}
		DataHandlerUtil.saveAllData();
		TownyProvinces.info("Province Prices Recalculated");
	}
	
}
