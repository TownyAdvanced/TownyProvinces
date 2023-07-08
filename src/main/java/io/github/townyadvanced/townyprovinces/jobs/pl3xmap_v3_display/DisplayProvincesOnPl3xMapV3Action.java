package io.github.townyadvanced.townyprovinces.jobs.pl3xmap_v3_display;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.province_generation.RegenerateRegionTask;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Stroke;
import net.pl3x.map.core.world.World;

import java.util.*;

public class DisplayProvincesOnPl3xMapV3Action {
	
	private SimpleLayer bordersLayer;
	private SimpleLayer homeBlocksLayer;
	private final TPFreeCoord tpFreeCoord;

	public DisplayProvincesOnPl3xMapV3Action() {
		TownyProvinces.info("Enabling Pl3xMap v3 support.");
		tpFreeCoord = new TPFreeCoord(0,0);
		TownyProvinces.info("Pl3xMap v3 support enabled.");
	}	

	/**
	 * Display all TownyProvinces items
	 */
	void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested) {
		if(bordersRefreshRequested) {
			if(bordersLayer != null) {
				bordersLayer.clearMarkers();
			}
			addProvinceBordersLayer();
		}
		if(homeBlocksRefreshRequested) {
			if(homeBlocksLayer != null) {
				homeBlocksLayer.clearMarkers();
			}
			addProvinceHomeBlocksLayer();
		}
		drawProvinceHomeBlocks();
		drawProvinceBorders();
	}
	
	private void addProvinceHomeBlocksLayer() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_town_costs").translate(Locale.ROOT);		
		homeBlocksLayer = createLayer("townyprovinces.markerset.homeblocks", name, true, 
			TownyProvincesSettings.getTownCostsLayerPriority(),
			TownyProvincesSettings.getTownCostsLayerZIndex(),
			TownyProvincesSettings.getTownCostsLayerIsToggleable());
	}

	private void addProvinceBordersLayer() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_borders").translate(Locale.ROOT);
		bordersLayer = createLayer("townyprovinces.markerset.borders", name, false, 
			TownyProvincesSettings.getProvincesLayerPriority(), 
			TownyProvincesSettings.getProvincesLayerZIndex(), 
			TownyProvincesSettings.getProvincesLayerIsToggleable());
	}
	
	private SimpleLayer createLayer(String layerKey, String layerName, boolean hideByDefault, int priority, int zIndex, boolean showControls) {
		//Create simple layer
		World world = Pl3xMap.api().getWorldRegistry().get(TownyProvincesSettings.getWorldName());
		if (world == null) {
			TownyProvinces.severe("World is not in Pl3xMap registry!");
			return null;
		}
		
		SimpleLayer layer = new SimpleLayer(layerKey, layerName::toString);

		layer.setDefaultHidden(hideByDefault);
		layer.setPriority(priority);
		layer.setZIndex(zIndex);
		layer.setShowControls(showControls);

		world.getLayerRegistry().register(layer);

		return layer;
	}
	
	private void drawProvinceHomeBlocks() {
		String border_icon_id = "provinces_costs_icon";
		boolean biomeCostAdjustmentsEnabled = TownyProvincesSettings.isBiomeCostAdjustmentsEnabled();
		Set<Province> copyOfProvincesSet = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet());
		for (Province province : copyOfProvincesSet) {
			try {
				TPCoord homeBlock = province.getHomeBlock();
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				Marker<?> homeBlockMarker = homeBlocksLayer.registeredMarkers().get(homeBlockMarkerId);

				if(province.isSea()) {
					//This is sea. If the marker is there, we need to remove it
					if(homeBlockMarker == null)
						continue;
					homeBlocksLayer.removeMarker(homeBlockMarkerId);
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
					
					homeBlockMarker = new Icon(
						homeBlockMarkerId, realHomeBlockX, realHomeBlockZ,
						border_icon_id,
						TownyProvincesSettings.getTownCostsIconWidth(), 
						TownyProvincesSettings.getTownCostsIconHeight());

					Options markerOptions = Options.builder()
						.popupContent(markerLabel)
						.tooltipContent(markerLabel)
						.build();

					homeBlockMarker.setOptions(markerOptions);
					
					homeBlocksLayer.addMarker(homeBlockMarker);
				}
			} catch (Exception ex) {
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}

	private void drawProvinceBorders() {
		//Find and draw the borders around each province
		for (Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
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
		Marker<?> polyLineMarker = bordersLayer.registeredMarkers().get(markerId);
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
			if (polyLineMarker.getOptions() == null) {
				TownyProvinces.severe("WARNING: Marker options are null for province border marker " + markerId + ".");
			}
			if (polyLineMarker.getOptions().getStroke() == null) {
				TownyProvinces.severe("WARNING: Marker stroke is null for province border marker " + markerId + ".");
			}
			Stroke stroke = polyLineMarker.getOptions().getStroke();
			if (stroke.getColor() == null) {
				TownyProvinces.severe("WARNING: Marker stroke color is null for province border marker " + markerId + ".");
			}
			//Re-evaluate colour
			if (province.isSea()) {
				if (stroke.getColor().equals(TownyProvincesSettings.getSeaProvinceBorderColour())) {
					//Change colour of marker
					stroke.setColor(TownyProvincesSettings.getSeaProvinceBorderColour());
					stroke.setWeight(TownyProvincesSettings.getSeaProvinceBorderWeight());
					//Does not support opacity
				}
			} else {
				if (!stroke.getColor().equals(TownyProvincesSettings.getLandProvinceBorderColour())) {
					//Change colour of marker
					stroke.setColor(TownyProvincesSettings.getLandProvinceBorderColour());
					stroke.setWeight(TownyProvincesSettings.getLandProvinceBorderWeight());
					//Does not support opacity
				}
			}
		} 
	}

	private List<TPCoord> arrangeBorderCoordsIntoDrawableLine(Set<TPCoord> unprocessedBorderCoords) {
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

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, String markerId) {
		int landBorderColour = TownyProvincesSettings.getLandProvinceBorderColour();
		int landBorderWeight = TownyProvincesSettings.getLandProvinceBorderWeight();
		int seaProvinceBorderColour = TownyProvincesSettings.getSeaProvinceBorderColour();
		int seaProvinceBorderWeight = TownyProvincesSettings.getSeaProvinceBorderWeight();
		
		List<Point> points = new ArrayList<>();
		for (TPCoord drawableLineOfBorderCoord : drawableLineOfBorderCoords) {
			int x = (drawableLineOfBorderCoord.getX() * TownyProvincesSettings.getChunkSideLength());
			int z = (drawableLineOfBorderCoord.getZ() * TownyProvincesSettings.getChunkSideLength());

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
			calculatePullStrengthFromNearbyProvince(drawableLineOfBorderCoord, province, tpFreeCoord);
			if (tpFreeCoord.getX() < 0) {
				x += 7;
			} else if (tpFreeCoord.getX() > 0) {
				x += 9;
			}
			if (tpFreeCoord.getZ() < 0) {
				z += 7;
			} else if (tpFreeCoord.getZ() > 0) {
				z += 9;
			}

			points.add(Point.of(x, z));
		}

		//Draw border line
		Polyline polyLineMarker = new Polyline(
			markerId, points);
		
		//Set colour
		Stroke stroke;
		if (province.isSea()) {
			stroke = new Stroke(seaProvinceBorderWeight, seaProvinceBorderColour);
		} else {
			stroke = new Stroke(landBorderWeight, landBorderColour);
		}
		
		Options markerOptions = Options.builder()
			.stroke(stroke)
			.build();
		
		polyLineMarker.setOptions(markerOptions);
		
		bordersLayer.addMarker(polyLineMarker);
	}
	
	private void calculatePullStrengthFromNearbyProvince(TPCoord borderCoordBeingPulled, Province provinceDoingThePulling, TPFreeCoord freeCoord) {
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
		freeCoord.setValues(pullStrengthX,pullStrengthZ);
	}
	
	////////////////////////// DEBUG SECTION ////////////////////////



	private void debugDrawProvinceChunks(Province province) {
		for(TPCoord tpCoord: TownyProvincesDataHolder.getInstance().getListOfCoordsInProvince(province)) {
			debugDrawChunk(tpCoord, province);
		}
	}
	
	
	//Shows all borders. But not for production
	private void debugDrawProvinceBorders() {
		//for (Coord coord : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
		//	debugDrawProvinceBorderBlock(worldName, provinceBlock);
		//}
	}
	
	private void debugDrawChunk(TPCoord coord, Province province) {
		double[] xPoints = new double[5];
		xPoints[0] = coord.getX() * TownyProvincesSettings.getChunkSideLength();
		xPoints[1] = xPoints[0] + TownyProvincesSettings.getChunkSideLength();
		xPoints[2] = xPoints[1];
		xPoints[3] = xPoints[0];
		xPoints[4] = xPoints[0];

		double[] zPoints = new double[5];
		zPoints[0] = coord.getZ() * TownyProvincesSettings.getChunkSideLength();
		zPoints[1] = zPoints[0]; 
		zPoints[2] = zPoints[1] + TownyProvincesSettings.getChunkSideLength();
		zPoints[3] = zPoints[2];
		zPoints[4] = zPoints[0];
		
		//This is not the ideal way to do this; but it simplifies it for debugging between platforms.
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < xPoints.length; i++) {
			points.add(Point.of(xPoints[i], zPoints[i]));
		}
		
		
		String markerId = "Debug Drawn Chunk" + coord.getX() + "-" + coord.getZ() + ". Province: " + province.getId();
		//String markerName = "xx";
		String markerName = "ID: " + markerId;
		//markerName += " Is Border: " + provinceBlock.isProvinceBorder();
		
		//AreaMarker areaMarker = markerSet.createAreaMarker(
		//	markerId, markerName, unknown, worldName,
	//		xPoints, zPoints, unknown2);
		
		Polyline polyLineMarker = new Polyline(
			markerId, points);
		
		Options markerOptions = Options.builder()
			.stroke(new Stroke(4, 0xff0000))
			.tooltipContent(markerName)
			.build();

		polyLineMarker.setOptions(markerOptions);
		
		bordersLayer.addMarker(polyLineMarker);
//polyLineMarker.setLineStyle(4,1, 300000);
//polyLineMarker.set
		//areaMarker.setFillStyle(0, 300000);
		//areaMarker.setLineStyle(1, 0.2, 300000);
	}
	 
}
