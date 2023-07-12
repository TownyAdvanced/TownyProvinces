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
	 * 1. Cycle each provinces
	 * 2. In each province, cycle each coord
	 * 3. For each coord, determine what is the relevant region
	 * 4. Whatever it is, add the total province costs
	 * 5. When all chunks are cycled, set the province costs
	 */
	public static void recalculateProvincePrices() {
		TownyProvinces.info("Recalculating province prices");
		double newTownCost;
		double upkeepTownCost;
		Region region;
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			newTownCost = 0;
			upkeepTownCost = 0;
			for(TPCoord coord: province.getListOfCoordsInProvince()) {
				region = TownyProvincesSettings.findPriceGoverningRegion(coord);
				newTownCost += region.getNewTownCostPerChunk();
				upkeepTownCost += region.getUpkeepTownCostPerChunk();
			}
			province.setNewTownCost(newTownCost);
			province.setUpkeepTownCost(upkeepTownCost);
		}
		DataHandlerUtil.saveAllData();
		TownyProvinces.info("Province Prices Recalculated");
	}
	
}
