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
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Fill;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Stroke;
import net.pl3x.map.core.world.World;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class DisplayProvincesOnPl3xMapV3Action extends DisplayProvincesOnMapAction implements EventListener {
	
	private SimpleLayer bordersLayer;
	private SimpleLayer homeBlocksLayer;
	private final TPFreeCoord tpFreeCoord;
	private World world;

	public DisplayProvincesOnPl3xMapV3Action() {
		TownyProvinces.info("Enabling Pl3xMap v3 support.");
		
		tpFreeCoord = new TPFreeCoord(0,0);
		
		reloadAction();

		Pl3xMap.api().getEventRegistry().register(this);
		
		TownyProvinces.info("Pl3xMap v3 support enabled.");
	}

	@EventHandler
	public void onPl3xMapEnabled(Pl3xMapEnabledEvent event) {
		reloadAction();
	}
	
	@EventHandler
	public void onPl3xMapWorldLoaded(WorldLoadedEvent event) {
		if (event.getWorld().getName().equals(TownyProvincesSettings.getWorldName())) {
			world = event.getWorld();
			world.getLayerRegistry().register(homeBlocksLayer);
			world.getLayerRegistry().register(bordersLayer);
		}
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
		
		if (bordersRefreshRequested) {
			if(bordersLayer != null) {
				bordersLayer.clearMarkers();
			}
			addProvinceBordersLayer();
		}
		if (homeBlocksRefreshRequested) {
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
			true);
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
		SimpleLayer simpleLayer = null;
		Layer layer = world.getLayerRegistry().get(layerKey);
		if (layer instanceof SimpleLayer) {
			simpleLayer = (SimpleLayer) layer;
		}
		
		if (simpleLayer == null) {
			simpleLayer = new SimpleLayer(layerKey, layerName::toString);
			world.getLayerRegistry().register(simpleLayer);
		}

		simpleLayer.setDefaultHidden(hideByDefault);
		simpleLayer.setPriority(priority);
		simpleLayer.setZIndex(zIndex);
		simpleLayer.setShowControls(showControls);
		
		return simpleLayer;
	}
	
	@Override
	void reloadAction() {
		BufferedImage configIcon = TownyProvincesSettings.getTownCostsIcon();
		
		if (configIcon == null) {
			throw new RuntimeException("Town Costs Icon is not a valid image");
		}

		Pl3xMap.api().getIconRegistry().register(new IconImage(
			"provinces_costs_icon", configIcon, "png"));
	}
	
	@Override
	protected void drawProvinceHomeBlocks() {
		String border_icon_id = "provinces_costs_icon";
		boolean biomeCostAdjustmentsEnabled = TownyProvincesSettings.isBiomeCostAdjustmentsEnabled();
		for (Province province : new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			try {
				TPCoord homeBlock = province.getHomeBlock();
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				Marker<?> homeBlockMarker = homeBlocksLayer.registeredMarkers().get(homeBlockMarkerId);

				if(province.getType().canNewTownsBeCreated()) {
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
				} else {
					//This is sea. If the marker is there, we need to remove it
					if(homeBlockMarker == null)
						continue;
					homeBlocksLayer.removeMarker(homeBlockMarkerId);
					return;
				}
			} catch (Exception ex) {
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	protected void drawProvinceBorder(Province province) {
		int borderColour = province.getType().getBorderColour() |
			(int)(255*province.getType().getBorderOpacity()) << 24;
		int borderWeight = province.getType().getBorderWeight();
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
				}
			}
		} else {
			if (polyLineMarker.getOptions() == null) {
				TownyProvinces.severe("WARNING: Marker options are null for province border marker " + markerId + ".");
				return;
			}
			if (polyLineMarker.getOptions().getStroke() == null) {
				TownyProvinces.severe("WARNING: Marker stroke is null for province border marker " + markerId + ".");
				return;
			}
			Stroke stroke = polyLineMarker.getOptions().getStroke();
			//Re-evaluate colour
			stroke.setColor(borderColour);
			stroke.setWeight(borderWeight);
		} 
	}

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, String markerId) {
		int borderColour = province.getType().getBorderColour() |
			(int)(255*province.getType().getBorderOpacity()) << 24;
		int borderWeight = province.getType().getBorderWeight();

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
		Stroke stroke = new Stroke(true)
			.setColor(borderColour)
			.setWeight(borderWeight);
		
		Options markerOptions = Options.builder()
			.stroke(stroke)
			.fill(new Fill(0))
			.build();
		
		polygonMarker.setOptions(markerOptions);

		bordersLayer.addMarker(new Polygon(markerId, polyLine).setOptions(markerOptions));
	}

	protected void setProvinceMapStyles() {
		int requiredBorderColour;
		int requiredBorderWeight;
		int requiredFillColour;
		Stroke stroke;
		Fill fill;
		String markerId;
		//Cycle provinces
		for(Province province: new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			//Set styles if needed
			markerId = province.getId();
			Marker<?> polyLineMarker = bordersLayer.registeredMarkers().get(markerId);
			if (polyLineMarker == null) {
				continue;
			}
			if (polyLineMarker.getOptions() == null) {
				TownyProvinces.severe("WARNING: Marker options are null for province border marker " + markerId + ".");
				continue;
			}
			if (polyLineMarker.getOptions().getStroke() == null) {
				TownyProvinces.severe("WARNING: Marker stroke is null for province border marker " + markerId + ".");
				continue;
			}
			stroke = polyLineMarker.getOptions().getStroke();
			if (stroke.getColor() == null) {
				TownyProvinces.severe("WARNING: Marker stroke color is null for province border marker " + markerId + ".");
				continue;
			}
			fill = polyLineMarker.getOptions().getFill();
			if (fill == null) {
				TownyProvinces.severe("WARNING: Marker fill is null for province border marker " + markerId + ".");
				continue;
			}
			if (fill.getColor() == null) {
				TownyProvinces.severe("WARNING: Marker fill color is null for province border marker " + markerId + ".");
				continue;
			}
			//Set border colour
			requiredBorderColour = province.getType().getBorderColour() |
				(int) (255 * province.getType().getBorderOpacity()) << 24;
			requiredBorderWeight = province.getType().getBorderWeight();
			stroke.setColor(requiredBorderColour);
			stroke.setWeight(requiredBorderWeight);
			//Set fill colour
			requiredFillColour = province.getFillColour() |
				(int) (255 * province.getFillOpacity()) << 24;
			fill.setColor(requiredFillColour);
		}
	}
}
