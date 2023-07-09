package io.github.townyadvanced.townyprovinces.jobs.map_display;

import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;

import java.util.HashSet;
import java.util.Set;

public abstract class DisplayProvincesOnMapAction {

	/**
	 * Display all TownyProvinces items
	 */
	abstract void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested);

	/**
	 * Find the border coords around the given province
	 *
	 * Note that these co-cords will not actually belong to the province
	 */
	public static Set<TPCoord> findAllBorderCoords(Province province) {
		Set<TPCoord> resultSet = new HashSet<>();
		for(TPCoord provinceCoord: province.getListOfCoordsInProvince()) {
			resultSet.addAll(province.getAdjacentBorderCoords(provinceCoord));
		}
		return resultSet;
	}
}
