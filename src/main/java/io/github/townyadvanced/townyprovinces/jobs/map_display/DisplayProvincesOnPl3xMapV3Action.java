package io.github.townyadvanced.townyprovinces.jobs.map_display;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Fill;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Stroke;
import net.pl3x.map.core.world.World;

import java.util.*;
import java.util.List;

public class DisplayProvincesOnPl3xMapV3Action extends DisplayProvincesOnMapAction {
	
	private SimpleLayer bordersLayer;
	private SimpleLayer homeBlocksLayer;
	private final TPFreeCoord tpFreeCoord;
	private World world;

	public DisplayProvincesOnPl3xMapV3Action() {
		TownyProvinces.info("Enabling Pl3xMap v3 support.");
		tpFreeCoord = new TPFreeCoord(0,0);
		
		if (TownyProvincesSettings.getTownCostsIcon() == null) {
			TownyProvinces.severe("Error: Town Costs Icon is not valid. Unable to support Pl3xMap V3.");
			return;
		}

		Pl3xMap.api().getIconRegistry().register(new IconImage(
			"provinces_costs_icon", TownyProvincesSettings.getTownCostsIcon(), "png"));
		
		TownyProvinces.info("Pl3xMap v3 support enabled.");
	}

	/**
	 * Display all TownyProvinces items
	 */
	void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested) {
		world = Pl3xMap.api().getWorldRegistry().get(TownyProvincesSettings.getWorldName());
		if (world == null) {
			TownyProvinces.severe("World is not in Pl3xMap registry!");
			return;
		}
		
		bordersLayer = (SimpleLayer) world.getLayerRegistry().get("townyprovinces.layer.borders");
		homeBlocksLayer = (SimpleLayer) world.getLayerRegistry().get("townyprovinces.layer.homeblocks");
		
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
		homeBlocksLayer = createLayer("townyprovinces.layer.homeblocks", name, true, 
			TownyProvincesSettings.getPl3xMapTownCostsLayerPriority(),
			TownyProvincesSettings.getPl3xMapTownCostsLayerZIndex(),
			TownyProvincesSettings.getPl3xMapTownCostsLayerIsToggleable());
	}

	private void addProvinceBordersLayer() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_borders").translate(Locale.ROOT);
		bordersLayer = createLayer("townyprovinces.layer.borders", name, false, 
			TownyProvincesSettings.getPl3xMapProvincesLayerPriority(), 
			TownyProvincesSettings.getPl3xMapProvincesLayerZIndex(), 
			TownyProvincesSettings.getPl3xMapProvincesLayerIsToggleable());
	}
	
	private SimpleLayer createLayer(String layerKey, String layerName, boolean hideByDefault, int priority, int zIndex, boolean showControls) {
		//Create simple layer
		if (world.getLayerRegistry().get(layerKey) != null)
			return (SimpleLayer)world.getLayerRegistry().get(layerKey);
		
		SimpleLayer layer = new SimpleLayer(layerKey, layerName::toString);

		layer.setDefaultHidden(hideByDefault);
		layer.setPriority(priority);
		layer.setZIndex(zIndex);
		layer.setShowControls(showControls);

		world.getLayerRegistry().register(layer);

		return layer;
	}
	
	@Override
	protected void drawProvinceHomeBlocks() {
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
	
	@Override
	protected void drawProvinceBorder(Province province) {
		int landBorderColour = TownyProvincesSettings.getLandProvinceBorderColour() +
			(int)(255*TownyProvincesSettings.getLandProvinceBorderOpacity()) << 24;
		int landBorderWeight = TownyProvincesSettings.getLandProvinceBorderWeight();
		int seaProvinceBorderColour = TownyProvincesSettings.getSeaProvinceBorderColour() +
			(int)(255*TownyProvincesSettings.getSeaProvinceBorderOpacity()) << 24;
		int seaProvinceBorderWeight = TownyProvincesSettings.getSeaProvinceBorderWeight();
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
				if (!stroke.getColor().equals(seaProvinceBorderColour)) {
					//Change colour of marker
					stroke.setColor(seaProvinceBorderColour);
					stroke.setWeight(seaProvinceBorderWeight);
					//Does not support opacity
				}
			} else {
				if (!stroke.getColor().equals(landBorderColour)) {
					//Change colour of marker
					stroke.setColor(landBorderColour);
					stroke.setWeight(landBorderWeight);
					//Does not support opacity
				}
			}
		} 
	}

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, String markerId) {
		int landBorderColour = TownyProvincesSettings.getLandProvinceBorderColour() +
			(int)(255*TownyProvincesSettings.getLandProvinceBorderOpacity()) << 24;
		int landBorderWeight = TownyProvincesSettings.getLandProvinceBorderWeight();
		int seaProvinceBorderColour = TownyProvincesSettings.getSeaProvinceBorderColour() +
			(int)(255*TownyProvincesSettings.getSeaProvinceBorderOpacity()) << 24;
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
		Polyline polyLine = new Polyline(markerId, points);

		//Convert line to polygon for display
		Polygon polygonMarker  = new Polygon("polygon_"+markerId, polyLine);
		
		//Set colour
		Stroke stroke = new Stroke(true);
		if (province.isSea()) {
			stroke.setColor(seaProvinceBorderColour);
			stroke.setWeight(seaProvinceBorderWeight);
		} else {
			stroke.setColor(landBorderColour);
			stroke.setWeight(landBorderWeight);
		}
		
		Options markerOptions = Options.builder()
			.stroke(stroke)
			.fill(new Fill(false))
			.build();
		
		polygonMarker.setOptions(markerOptions);

		bordersLayer.addMarker(new Polygon(markerId, polyLine).setOptions(markerOptions));
	}
	
	////////////////////////// DEBUG SECTION ////////////////////////
	
	
	//Shows all borders. But not for production
	@Override
	protected void debugDrawProvinceBorders() {
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
			.stroke(new Stroke(4, 0xffff0000))
			.fill(new Fill(false))
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
