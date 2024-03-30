package io.github.townyadvanced.townyprovinces.jobs.map_display;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Polygon;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DisplayProvincesOnSquaremapAction extends DisplayProvincesOnMapAction {
	
	private final Squaremap api;
	private SimpleLayerProvider bordersLayer;
	private SimpleLayerProvider homeBlocksLayer;
	private final TPFreeCoord tpFreeCoord;
	private final Key iconKey = Key.of("provinces_costs_icon");
	private final Key bordersKey = Key.of("townyprovinces.markerset.borders");
	private final Key homeBlocksKey = Key.of("townyprovinces.markerset.homeblocks");
	private MapWorld world;

	public DisplayProvincesOnSquaremapAction() {
		TownyProvinces.info("Enabling Squaremap support.");
		
		api = SquaremapProvider.get();
		tpFreeCoord = new TPFreeCoord(0,0);

		reloadAction();
				
		TownyProvinces.info("Squaremap support enabled.");
	}

	/**
	 * Display all TownyProvinces items
	 */
	void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested) {
		World bukkitWorld = Bukkit.getWorld(TownyProvincesSettings.getWorldName());
		if (bukkitWorld == null) {
			TownyProvinces.severe("Configured world is not a valid world!");
			return;
		}
		world = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(bukkitWorld)).orElse(null);
		if (world == null) {
			TownyProvinces.severe("World is not in Pl3xMap registry!");
			return;
		}
		
		if (world.layerRegistry().hasEntry(bordersKey))
			bordersLayer = (SimpleLayerProvider) world.layerRegistry().get(bordersKey);
		if (world.layerRegistry().hasEntry(homeBlocksKey))
			homeBlocksLayer = (SimpleLayerProvider) world.layerRegistry().get(homeBlocksKey);
		
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
		homeBlocksLayer = createLayer(homeBlocksKey, name, true, 
			TownyProvincesSettings.getPl3xMapTownCostsLayerPriority(),
			TownyProvincesSettings.getPl3xMapTownCostsLayerZIndex(),
			true);
	}

	private void addProvinceBordersLayer() {
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_borders").translate(Locale.ROOT);
		bordersLayer = createLayer(bordersKey, name, false, 
			TownyProvincesSettings.getPl3xMapProvincesLayerPriority(), 
			TownyProvincesSettings.getPl3xMapProvincesLayerZIndex(), 
			TownyProvincesSettings.getPl3xMapProvincesLayerIsToggleable());
	}
	
	private SimpleLayerProvider createLayer(Key layerKey, String layerName, boolean hideByDefault, int priority, int zIndex, boolean showControls) {
		if (world.layerRegistry().hasEntry(layerKey)) {
			//Existing simple layer
			world.layerRegistry().unregister(layerKey);
		}
		//Create simple layer
		SimpleLayerProvider layer = SimpleLayerProvider.builder(layerName)
			.defaultHidden(hideByDefault)
			.layerPriority(priority)
			.zIndex(zIndex)
			.showControls(showControls)
			.build();

		world.layerRegistry().register(layerKey, layer);

		return layer;
	}

	@Override
	void reloadAction() {
		if (TownyProvincesSettings.getTownCostsIcon() == null) {
			throw new RuntimeException("Town Costs Icon URL is not a valid image link");
		}
		
		if (api.iconRegistry().hasEntry(iconKey)) api.iconRegistry().unregister(iconKey);
		api.iconRegistry().register(iconKey, TownyProvincesSettings.getTownCostsIcon());
	}
	
	@Override
	protected void drawProvinceHomeBlocks() {
		boolean biomeCostAdjustmentsEnabled = TownyProvincesSettings.isBiomeCostAdjustmentsEnabled();
		for (Province province : new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			try {
				TPCoord homeBlock = province.getHomeBlock();
				Key homeBlockMarkerKey = Key.of("province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ());
				Marker homeBlockMarker = homeBlocksLayer.registeredMarkers().get(homeBlockMarkerKey);

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
					
					homeBlockMarker = Marker.icon(
						Point.of(realHomeBlockX, realHomeBlockZ),
						iconKey,
						TownyProvincesSettings.getTownCostsIconWidth(), 
						TownyProvincesSettings.getTownCostsIconHeight());

					MarkerOptions markerOptions = MarkerOptions.builder()
						.clickTooltip(markerLabel)
						.hoverTooltip(markerLabel)
						.build();

					homeBlockMarker.markerOptions(markerOptions);
					
					homeBlocksLayer.addMarker(homeBlockMarkerKey, homeBlockMarker);
				} else {
					//This is sea. If the marker is there, we need to remove it
					if(homeBlockMarker == null)
						continue;
					homeBlocksLayer.removeMarker(homeBlockMarkerKey);
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
		Color borderColor = new Color(province.getType().getBorderColour());
		double borderOpacity = province.getType().getBorderOpacity();
		int borderWeight = province.getType().getBorderWeight();
		Key markerKey = Key.of(province.getId());
		Marker polyLineMarker = bordersLayer.registeredMarkers().get(markerKey);
		if(polyLineMarker == null) {
			//Get border blocks
			Set<TPCoord> borderCoords = findAllBorderCoords(province);
			if(borderCoords.size() > 0) {
				//Arrange border blocks into drawable line
				List<TPCoord> drawableLineOfBorderCoords = arrangeBorderCoordsIntoDrawableLine(borderCoords);
				
				//Draw line
				if(drawableLineOfBorderCoords.size() > 0) {
					drawBorderLine(drawableLineOfBorderCoords, province, markerKey);
				} else {
					TownyProvinces.severe("WARNING: Could not arrange province coords into drawable line. If this message has not stopped repeating a few minutes after your server starts, please report it to TownyAdvanced.");
				}
			}
		} else {
			MarkerOptions oldOptions = polyLineMarker.markerOptions();
			//Change colour of marker - Don't know why it can't be modified directly
			MarkerOptions.Builder markerOptions = MarkerOptions.builder()
				.stroke(oldOptions.stroke())
				.strokeColor(borderColor)
				.strokeWeight(borderWeight)
				.strokeOpacity(borderOpacity)
				.fill(oldOptions.fill())
				.fillOpacity(oldOptions.fillOpacity())
				.fillRule(oldOptions.fillRule())
				.clickTooltip(oldOptions.clickTooltip())
				.hoverTooltip(oldOptions.hoverTooltip());
			if (oldOptions.fillColor() != null)
				markerOptions.fillColor(oldOptions.fillColor());
			polyLineMarker.markerOptions(markerOptions);
		} 
	}

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, Key markerKey) {
		Color borderColor = new Color(province.getType().getBorderColour());
		double borderOpacity = province.getType().getBorderOpacity();
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

		//Convert line to polygon for display
		Polygon polygonMarker  = Marker.polygon(points);
		
		//Set colour
		MarkerOptions markerOptions = MarkerOptions.builder()
			.strokeColor(borderColor)
			.strokeWeight(borderWeight)
			.strokeOpacity(borderOpacity)
			.build();
		
		polygonMarker.markerOptions(markerOptions);

		bordersLayer.addMarker(markerKey, polygonMarker);
	}

	protected void setProvinceMapStyles() {
		Color requiredBorderColor;
		int requiredBorderWeight;
		double requiredBorderOpacity;
		Color requiredFillColor;
		double requiredFillOpacity;
		Key markerKey;
		//Cycle provinces
		for(Province province: new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			//Set styles if needed
			markerKey = Key.of(province.getId());
			Marker polygonMarker = bordersLayer.registeredMarkers().get(markerKey);
			if (polygonMarker == null) {
				continue;
			}
			//Set border colour if needed
			requiredBorderColor = new Color(province.getType().getBorderColour());
			requiredBorderWeight = province.getType().getBorderWeight();
			requiredBorderOpacity = province.getType().getBorderOpacity();
			
			requiredFillColor = new Color(province.getFillColour());
			requiredFillOpacity = province.getFillOpacity();
			
			MarkerOptions oldOptions = polygonMarker.markerOptions();
			MarkerOptions.Builder markerOptions = MarkerOptions.builder()
				.stroke(oldOptions.stroke())
				.strokeColor(requiredBorderColor)
				.strokeWeight(requiredBorderWeight)
				.strokeOpacity(requiredBorderOpacity)
				.fill(oldOptions.fill())
				.fillColor(requiredFillColor)
				.fillOpacity(requiredFillOpacity)
				.fillRule(oldOptions.fillRule())
				.clickTooltip(oldOptions.clickTooltip())
				.hoverTooltip(oldOptions.hoverTooltip());
			
			polygonMarker.markerOptions(markerOptions);
		}
	}
}
