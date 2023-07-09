package io.github.townyadvanced.townyprovinces.jobs.map_display;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DisplayProvincesOnDynmapAction extends DisplayProvincesOnMapAction {
	
	private final MarkerAPI markerapi;
	private MarkerSet bordersMarkerSet;
	private MarkerSet homeBlocksMarkerSet;
	private final TPFreeCoord tpFreeCoord;

	public DisplayProvincesOnDynmapAction() {
		TownyProvinces.info("Enabling dynmap support.");
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
		markerapi = dynmapAPI.getMarkerAPI();
		tpFreeCoord = new TPFreeCoord(0,0);
		TownyProvinces.info("Dynmap support enabled.");
	}
	
	@Override
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
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_town_costs").translate(Locale.ROOT);		
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
	
	@Override
	protected void drawProvinceHomeBlocks() {
		String border_icon_id = "coins";
		boolean biomeCostAdjustmentsEnabled = TownyProvincesSettings.isBiomeCostAdjustmentsEnabled();
		MarkerIcon homeBlockIcon = markerapi.getMarkerIcon(border_icon_id);
		Set<Province> copyOfProvincesSet = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet());
		for (Province province : copyOfProvincesSet) {
			try {
				TPCoord homeBlock = province.getHomeBlock();
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				Marker homeBlockMarker = homeBlocksMarkerSet.findMarker(homeBlockMarkerId);

				if(province.isSea()) {
					//This is sea. If the marker is there, we need to remove it
					if(homeBlockMarker == null)
						continue;
					homeBlockMarker.deleteMarker();
					return;
				} else {
					//This is land If the marker is not there, we need to add it
					if(homeBlockMarker != null)
						continue;
					int realHomeBlockX = homeBlock.getX() * TownyProvincesSettings.getChunkSideLength();
					int realHomeBlockZ = homeBlock.getZ() * TownyProvincesSettings.getChunkSideLength();

					String markerLabel;
					if(TownyEconomyHandler.isActive()) {
						int newTownCost = (int)(biomeCostAdjustmentsEnabled ? province.getBiomeAdjustedNewTownCost() : province.getNewTownCost());
						String newTownCostString = TownyEconomyHandler.getFormattedBalance(newTownCost);
						int upkeepTownCost = (int)(biomeCostAdjustmentsEnabled ? province.getBiomeAdjustedUpkeepTownCost() : province.getUpkeepTownCost());
						String upkeepTownCostString = TownyEconomyHandler.getFormattedBalance(upkeepTownCost);
						markerLabel = Translatable.of("dynmap_province_homeblock_label", newTownCostString, upkeepTownCostString).translate(Locale.ROOT);
					} else {
						markerLabel = "";
					}
					
					homeBlockMarker = homeBlocksMarkerSet.createMarker(
						homeBlockMarkerId, markerLabel, TownyProvincesSettings.getWorldName(),
						realHomeBlockX, 64, realHomeBlockZ,
						homeBlockIcon, true);
					homeBlockMarker.setDescription(markerLabel);
				}
			} catch (Exception ex) {
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	protected void drawProvinceBorder(Province province) {
		String markerId = province.getId();
		PolyLineMarker polyLineMarker = bordersMarkerSet.findPolyLineMarker(markerId);
		if(polyLineMarker == null) {
			//Get border blocks
			Set<TPCoord> borderCoords = findAllBorderCoords(province);
			if(borderCoords.size() > 0) {
				//Arrange border blocks into drawable line
				List<TPCoord> drawableLineOfBorderCoords = arrangeBorderCoordsIntoDrawableLine(borderCoords);
				
				//Draw line
				if(drawableLineOfBorderCoords.size() > 0) {
					drawBorderLine(drawableLineOfBorderCoords, province, markerId);
				} else {
					TownyProvinces.severe("WARNING: Could not arrange province coords into drawable line. If this message has not stopped repeating a few minutes after your server starts, please report it to TownyAdvanced.");
					//The below line will draw the province if uncommented
					//debugDrawProvinceChunks(province);
				}
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
			calculatePullStrengthFromNearbyProvince(drawableLineOfBorderCoords.get(i), province, tpFreeCoord);
			if (tpFreeCoord.getX() < 0) {
				xPoints[i] = xPoints[i] + 7;
			} else if (tpFreeCoord.getX() > 0) {
				xPoints[i] = xPoints[i] + 9;
			}
			if (tpFreeCoord.getZ() < 0) {
				zPoints[i] = zPoints[i] + 7;
			} else if (tpFreeCoord.getZ() > 0) {
				zPoints[i] = zPoints[i] + 9;
			}
		}

		boolean unknown = false;
		boolean unknown2 = false;

		//Draw border line
		PolyLineMarker polyLineMarker = bordersMarkerSet.createPolyLineMarker(
			markerId, null, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);
		
		//Set colour
		if (province.isSea()) {
			polyLineMarker.setLineStyle(seaProvinceBorderWeight, seaProvinceBorderOpacity, seaProvinceBorderColour);
		} else {
			polyLineMarker.setLineStyle(landBorderWeight, landBorderOpacity, landBorderColour);
		}
	}
	
	////////////////////////// DEBUG SECTION ////////////////////////

	
	//Shows all borders. But not for production
	@Override
	protected void debugDrawProvinceBorders() {
		String worldName = TownyProvincesSettings.getWorldName();

		//for (Coord coord : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
		//	debugDrawProvinceBorderBlock(worldName, provinceBlock);
		//}
	}
	
	@Override
	protected void debugDrawChunk(TPCoord coord, Province province, String worldName) {
		double[] xPoints = new double[5];
		xPoints[0] = coord.getX() * TownyProvincesSettings.getChunkSideLength();
		xPoints[1] = xPoints[0] + TownyProvincesSettings.getChunkSideLength();
		xPoints[2] = xPoints[1];
		xPoints[3] = xPoints[0];
		xPoints[4] = xPoints[0];

		double[] zPoints = new double[5];
		zPoints[0] = coord.getZ() * TownyProvincesSettings.getChunkSideLength();
		zPoints[1] = zPoints[0]; 
		zPoints[2] = zPoints[1] + TownyProvincesSettings.getChunkSideLength();;
		zPoints[3] = zPoints[2];
		zPoints[4] = zPoints[0];
		
		String markerId = "Debug Drawn Chunk" + coord.getX() + "-" + coord.getZ() + ". Province: " + province.getId();
		//String markerName = "xx";
		String markerName = "ID: " + markerId;
		//markerName += " Is Border: " + provinceBlock.isProvinceBorder();
		
		boolean unknown = false;
		boolean unknown2 = false;
		
		//AreaMarker areaMarker = markerSet.createAreaMarker(
		//	markerId, markerName, unknown, worldName,
	//		xPoints, zPoints, unknown2);

		PolyLineMarker polyLineMarker =  bordersMarkerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);

		if(polyLineMarker != null) {
			polyLineMarker.setLineStyle(4,1, 0xff0000);
		}
//polyLineMarker.setLineStyle(4,1, 300000);
//polyLineMarker.set
		//areaMarker.setFillStyle(0, 300000);
		//areaMarker.setLineStyle(1, 0.2, 300000);
	}
	 
}
