package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.province_generation.RegenerateRegionTask;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DisplayProvincesOnDynmapAction {
	
	private final MarkerAPI markerapi;
	private MarkerSet bordersMarkerSet;
	private MarkerSet homeBlocksMarkerSet;

	public DisplayProvincesOnDynmapAction() {
		TownyProvinces.info("Enabling dynmap support.");
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
		markerapi = dynmapAPI.getMarkerAPI();
		TownyProvinces.info("Dynmap support enabled.");
	}

	/**
	 * Display all TownyProvinces items
	 */
	void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested) {
		if(bordersRefreshRequested) {
			if(bordersMarkerSet != null) {
				bordersMarkerSet.deleteMarkerSet();
			}
			addProvinceBordersMarkerSet();
		}
		if(homeBlocksRefreshRequested) {
			if(homeBlocksMarkerSet != null) {
				homeBlocksMarkerSet.deleteMarkerSet();
			}
			addProvinceHomeBlocksMarkerSet();
		}
		drawProvinceHomeBlocks();
		drawProvinceBorders();
	}
	
	private void addProvinceHomeBlocksMarkerSet() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_prices").translate(Locale.ROOT);		
		homeBlocksMarkerSet = createMarkerSet("townyprovinces.markerset.homeblocks", name, true, false);
	}

	private void addProvinceBordersMarkerSet() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_borders").translate(Locale.ROOT);
		bordersMarkerSet = createMarkerSet("townyprovinces.markerset.borders", name, false, true);
	}
	
	private MarkerSet createMarkerSet(String markerSetId, String markerSetName, boolean hideByDefault, boolean labelShow) {
		if (markerapi == null) {
			TownyProvinces.severe("Error loading Dynmap marker API!");
			return null;
		}

		//Create marker set
		MarkerSet markerSet = markerapi.getMarkerSet(markerSetId);
		if (markerSet == null) {
			markerSet = markerapi.createMarkerSet(markerSetId, markerSetName, null, false);
			markerSet.setHideByDefault(hideByDefault);
			markerSet.setLabelShow(labelShow);
		}

		if (markerSet == null) {
			TownyProvinces.severe("Error creating Dynmap marker set!");
			return null;
		}
		return markerSet;
	}
	
	private void drawProvinceHomeBlocks() {
		String border_icon = "coins";
		for (Province province : TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			try {
				if(province.isSea())
					continue;
				TPCoord homeBlock = province.getHomeBlock();
				int realHomeBlockX = homeBlock.getX() * TownyProvincesSettings.getChunkSideLength();
				int realHomeBlockZ = homeBlock.getZ() * TownyProvincesSettings.getChunkSideLength();

				MarkerIcon homeBlockIcon = markerapi.getMarkerIcon(border_icon);
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				
				String newTownCost = TownyEconomyHandler.getFormattedBalance(province.getNewTownCost());
				String upkeepTownCost = TownyEconomyHandler.getFormattedBalance(province.getUpkeepTownCost());
				String markerLabel = Translatable.of("dynmap_province_homeblock_label", newTownCost, upkeepTownCost).translate(Locale.ROOT);
				Marker homeBlockMarker = homeBlocksMarkerSet.findMarker(homeBlockMarkerId);
				if (homeBlockMarker == null) {
					homeBlocksMarkerSet.createMarker(
						homeBlockMarkerId, markerLabel, TownyProvincesSettings.getWorldName(),
						realHomeBlockX, 64, realHomeBlockZ,
						homeBlockIcon, true);
					//homeBlockMarker.setLabel(markerLabel);
					//homeBlockMarker.setDescription(null);
				}
			} catch (Exception ex) {
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}

	private void drawProvinceBorders() {
		//Find and draw the borders around each province
		for (Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			try {
				drawProvinceBorder(province);
			} catch (Throwable t) {
				TownyProvinces.severe("Could not draw province borders for province at x " + province.getHomeBlock().getX() + " z " + province.getHomeBlock().getZ());
				t.printStackTrace();
			}
		}
	}
	
	private void drawProvinceBorder(Province province) {
		String markerId = province.getId();
		PolyLineMarker polyLineMarker = bordersMarkerSet.findPolyLineMarker(markerId);
		if(polyLineMarker == null) {
			//Get border blocks
			Set<TPCoord> borderCoords = findAllBorderCoords(province);
			if(borderCoords.size() > 0) {
				//Arrange border blocks into drawable line
				List<TPCoord> drawableLineOfBorderCoords = arrangeBorderCoordsIntoDrawableLine(borderCoords);
				//Draw line
				drawBorderLine(drawableLineOfBorderCoords, province, markerId);
			}
		} else {
			//Re-evaluate colour 
			if (province.isSea()) {
				if (polyLineMarker.getLineColor() != TownyProvincesSettings.getSeaProvinceBorderColour()) {
					//Change colour of marker
					polyLineMarker.setLineStyle(TownyProvincesSettings.getSeaProvinceBorderWeight(), TownyProvincesSettings.getSeaProvinceBorderOpacity(), TownyProvincesSettings.getSeaProvinceBorderColour());
				}
			} else {
				if (polyLineMarker.getLineColor() != TownyProvincesSettings.getLandProvinceBorderColour()) {
					//Change colour of marker
					polyLineMarker.setLineStyle(TownyProvincesSettings.getLandProvinceBorderWeight(), TownyProvincesSettings.getLandProvinceBorderOpacity(), TownyProvincesSettings.getLandProvinceBorderColour());
				}
			}
		} 
	}
	
	private List<TPCoord> arrangeBorderCoordsIntoDrawableLine(Set<TPCoord> unsortedBorderCoords) {
		List<TPCoord> result = new ArrayList<>();
		List<TPCoord> unProcessedCoords = new ArrayList<>(unsortedBorderCoords);
		TPCoord lineHead = (new ArrayList<>(unProcessedCoords)).get(0);
		TPCoord coordToAddToLine;
		while(unProcessedCoords.size() > 0) {
			//Cycle the list of unprocessed coords. Add the first one which suits then exit loop
			coordToAddToLine = null;
			for(TPCoord unprocessedCoord: unProcessedCoords) {
				if(areCoordsCardinallyAdjacent(unprocessedCoord, lineHead)) {
					coordToAddToLine = unprocessedCoord;
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
				unProcessedCoords.remove(coordToAddToLine);
			} else {
				TownyProvinces.severe("ERROR: Could not arrange province coords into drawable line");
				result.clear();
				return result;
			}

		}
		//Add last block to line, to make a circuit
		result.add(result.get(0));
		return result;
	}

	/**
	 * Find the border coords around the given province
	 * 
	 * Note that these co-cords will not actually belong to the province
	 */
	public static Set<TPCoord> findAllBorderCoords(Province province) {
		Set<TPCoord> resultSet = new HashSet<>();
		for(TPCoord provinceCoord: province.getCoordsInProvince()) {
			resultSet.addAll(province.getAdjacentBorderCoords(provinceCoord));
		}
		return resultSet;
	}

	private boolean areCoordsCardinallyAdjacent(TPCoord c1, TPCoord c2) {
		return TownyProvincesMathUtil.distance(c1,c2) == 1;
	}

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, String markerId) {
		String worldName = TownyProvincesSettings.getWorldName();
		int landBorderColour = TownyProvincesSettings.getLandProvinceBorderColour();
		int landBorderWeight = TownyProvincesSettings.getLandProvinceBorderWeight();
		double landBorderOpacity = TownyProvincesSettings.getLandProvinceBorderOpacity();
		int seaProvinceBorderColour = TownyProvincesSettings.getSeaProvinceBorderColour();
		int seaProvinceBorderWeight = TownyProvincesSettings.getSeaProvinceBorderWeight();
		double seaProvinceBorderOpacity = TownyProvincesSettings.getSeaProvinceBorderOpacity();

		double[] xPoints = new double[drawableLineOfBorderCoords.size()];
		double[] zPoints = new double[drawableLineOfBorderCoords.size()];
		for (int i = 0; i < drawableLineOfBorderCoords.size(); i++) {
			xPoints[i] = (drawableLineOfBorderCoords.get(i).getX() * TownyProvincesSettings.getChunkSideLength());
			zPoints[i] = (drawableLineOfBorderCoords.get(i).getZ() * TownyProvincesSettings.getChunkSideLength());

			/*
			 * At this point,the draw location is at the top left of the block.
			 * We need to move it towards the middle
			 *
			 * First we find the x,y pull strength from the nearby province
			 *
			 * Then we apply the following modifiers
			 * if x is negative, add 7
			 * if x is positive, add 9
			 * if z is negative, add 7
			 * if z is positive, add 9
			 *
			 * Result:
			 * 1. Each province border is inset from the chunk border by 6 blocks
			 * 2. The border on the sea takes the appearance of a single line
			 * 3. The border between 2 provinces takes the appearance of a double line,
			 *    with 2 blocks in between each line.
			 *
			 * NOTE ABOUT THE DOUBLE LINE:
			 * I was initially aiming for a single line but it might not be worth it because:
			 * 1. A double line has benefits:
			 *   - It's friendly to the processor
			 *   - It looks cool
			 *   - The single-line sea border looks like it was done on purpose
			 * 2. A single line has problems:
			 *   - If you simply bring the lines together, you'll probably get visual artefacts
			 *   - If you move the lines next to each other, you'll probably get visual artefacts
			 *   - If you try to do draw the lines using area markers, you'll increase processor load, and probably still get visual artefacts.
			 *   - On a sea border, the expected single line will either look slightly weaker or slightly thinner,
			 *     while will most likely appear to users as a bug.
			 * */
			TPCoord pullStrengthFromNearbyProvince = calculatePullStrengthFromNearbyProvince(drawableLineOfBorderCoords.get(i), province);
			if (pullStrengthFromNearbyProvince.getX() < 0) {
				xPoints[i] = xPoints[i] + 7;
			} else if (pullStrengthFromNearbyProvince.getX() > 0) {
				xPoints[i] = xPoints[i] + 9;
			}
			if (pullStrengthFromNearbyProvince.getZ() < 0) {
				zPoints[i] = zPoints[i] + 7;
			} else if (pullStrengthFromNearbyProvince.getZ() > 0) {
				zPoints[i] = zPoints[i] + 9;
			}
		}

		String markerName = "ID: " + markerId;

		boolean unknown = false;
		boolean unknown2 = false;

		//Draw border line
		PolyLineMarker polyLineMarker = bordersMarkerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);
		
		//Set colour
		if (province.isSea()) {
			polyLineMarker.setLineStyle(seaProvinceBorderWeight, seaProvinceBorderOpacity, seaProvinceBorderColour);
		} else {
			polyLineMarker.setLineStyle(landBorderWeight, landBorderOpacity, landBorderColour);
		}
	}
	
	private TPCoord calculatePullStrengthFromNearbyProvince(TPCoord borderCoordBeingPulled, Province provinceDoingThePulling) {
		int pullStrengthX = 0;
		int pullStrengthZ = 0;
		Set<TPCoord> adjacentCoords = RegenerateRegionTask.findAllAdjacentCoords(borderCoordBeingPulled);
		Province adjacenProvince;
		for(TPCoord adjacentCoord: adjacentCoords) {
			adjacenProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord.getX(), adjacentCoord.getZ());
			if(adjacenProvince != null && adjacenProvince.equals(provinceDoingThePulling)) {
				pullStrengthX += (adjacentCoord.getX() - borderCoordBeingPulled.getX());
				pullStrengthZ += (adjacentCoord.getZ() - borderCoordBeingPulled.getZ());
			}
		}
		return new TPCoord(pullStrengthX, pullStrengthZ);
	}
	
	////////////////////////// DEBUG SECTION ////////////////////////
	
	//Shows all borders. But not for production
	private void debugDrawProvinceBorders() {
		String worldName = TownyProvincesSettings.getWorldName();

		//for (Coord coord : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
		//	debugDrawProvinceBorderBlock(worldName, provinceBlock);
		//}
	}
	
	/*
	private void debugDrawProvinceBorderBlock(String worldName, ProvinceBlock provinceBlock) {
		double[] xPoints = new double[5];
		xPoints[0] = provinceBlock.getCoord().getX() * TownyProvincesSettings.getProvinceBlockSideLength();
		xPoints[1] = xPoints[0] + TownyProvincesSettings.getProvinceBlockSideLength();
		xPoints[2] = xPoints[1];
		xPoints[3] = xPoints[0];
		xPoints[4] = xPoints[0];

		double[] zPoints = new double[5];
		zPoints[0] = provinceBlock.getCoord().getZ() * TownyProvincesSettings.getProvinceBlockSideLength();
		zPoints[1] = zPoints[0]; 
		zPoints[2] = zPoints[1] + TownyProvincesSettings.getProvinceBlockSideLength();;
		zPoints[3] = zPoints[2];
		zPoints[4] = zPoints[0];
		
		String markerId = "border_province_block_" + provinceBlock.getCoord().getX() + "-" + provinceBlock.getCoord().getZ();
		//String markerName = "xx";
		String markerName = "ID: " + markerId;
		//markerName += " Is Border: " + provinceBlock.isProvinceBorder();
		
		boolean unknown = false;
		boolean unknown2 = false;
		
		//AreaMarker areaMarker = markerSet.createAreaMarker(
		//	markerId, markerName, unknown, worldName,
	//		xPoints, zPoints, unknown2);

		PolyLineMarker polyLineMarker =  markerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);

//polyLineMarker.setLineStyle(4,1, 300000);
//polyLineMarker.set
		//areaMarker.setFillStyle(0, 300000);
		//areaMarker.setLineStyle(1, 0.2, 300000);
	}
	*/
	 
}
