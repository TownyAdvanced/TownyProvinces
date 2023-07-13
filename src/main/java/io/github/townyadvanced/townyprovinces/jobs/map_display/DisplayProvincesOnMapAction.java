package io.github.townyadvanced.townyprovinces.jobs.map_display;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.province_generation.RegenerateRegionTask;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DisplayProvincesOnMapAction {

	/**
	 * Display all TownyProvinces items
	 */
	abstract void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested);

	abstract protected void drawProvinceHomeBlocks();

	protected void drawProvinceBorders() {
		//Find and draw the borders around each province
		for (Province province: new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			try {
				drawProvinceBorder(province);
			} catch (Throwable t) {
				TownyProvinces.severe("Could not draw province borders for province at x " + province.getHomeBlock().getX() + " z " + province.getHomeBlock().getZ());
				t.printStackTrace();
			}
		}
		//Recalculate province map styles
		TownyProvincesDataHolder.getInstance().recalculateProvinceMapStyles();
		//Set province map styles
		setProvinceMapStyles();
	}
	
	abstract protected void setProvinceMapStyles();
	
	abstract protected void drawProvinceBorder(Province province);

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

	protected List<TPCoord> arrangeBorderCoordsIntoDrawableLine(Set<TPCoord> unprocessedBorderCoords) {
		List<TPCoord> result = new ArrayList<>();
		TPCoord lineHead = null;
		for(TPCoord coord: unprocessedBorderCoords) {
			lineHead = coord;
			break;
		}
		TPCoord coordToAddToLine;
		while(unprocessedBorderCoords.size() > 0) {
			//Cycle the list of unprocessed coords. Add the first one which suits then exit loop
			coordToAddToLine = null;
			for(TPCoord unprocessedBorderCoord: unprocessedBorderCoords) {
				if(TownyProvincesMathUtil.areCoordsCardinallyAdjacent(unprocessedBorderCoord, lineHead)) {
					coordToAddToLine = unprocessedBorderCoord;
					break;
				}
			}

			/*
			 * If we found a coord to add to line. Add it
			 * Otherwise throw exception because we could not make a drawable line
			 */
			if(coordToAddToLine != null) {
				lineHead = coordToAddToLine;
				result.add(coordToAddToLine);
				unprocessedBorderCoords.remove(coordToAddToLine);
			} else {
				result.clear();
				return result;
			}
		}
		//Add last block to line, to make a circuit
		result.add(result.get(0));
		return result;
	}

	protected void calculatePullStrengthFromNearbyProvince(TPCoord borderCoordBeingPulled, Province provinceDoingThePulling, TPFreeCoord freeCoord) {
		int pullStrengthX = 0;
		int pullStrengthZ = 0;
		Set<TPCoord> adjacentCoords = RegenerateRegionTask.findAllAdjacentCoords(borderCoordBeingPulled);
		Province adjacenProvince;
		for(TPCoord adjacentCoord: adjacentCoords) {
			adjacenProvince = TownyProvincesDataHolder.getInstance().getProvinceAtCoord(adjacentCoord.getX(), adjacentCoord.getZ());
			if(adjacenProvince != null && adjacenProvince.equals(provinceDoingThePulling)) {
				pullStrengthX += (adjacentCoord.getX() - borderCoordBeingPulled.getX());
				pullStrengthZ += (adjacentCoord.getZ() - borderCoordBeingPulled.getZ());
			}
		}
		freeCoord.setValues(pullStrengthX,pullStrengthZ);
	}
	
}
