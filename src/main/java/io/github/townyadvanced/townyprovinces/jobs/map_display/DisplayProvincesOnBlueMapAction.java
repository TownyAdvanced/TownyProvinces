package io.github.townyadvanced.townyprovinces.jobs.map_display;

import com.flowpowered.math.vector.Vector2d;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.*;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;

public class DisplayProvincesOnBlueMapAction extends DisplayProvincesOnMapAction{
	private MarkerSet borderMarkerSet;
	private MarkerSet homeBlocksMarkersSet;
	private final TPFreeCoord tpFreeCoord;
	
	public DisplayProvincesOnBlueMapAction(){
		TownyProvinces.info("Enabling BlueMap support.");

		tpFreeCoord = new TPFreeCoord(0,0);
		
		if (TownyProvincesSettings.getTownCostsIcon() == null) {
			TownyProvinces.severe("Error: Town Costs Icon is not valid. Unable to support BlueMap.");
			return;
		}
		
		BlueMapAPI.onEnable(e -> {
			Path assetsFolder = e.getWebApp().getWebRoot().resolve("assets");
			try (OutputStream out = Files.newOutputStream(assetsFolder.resolve("province.png"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				BufferedImage configIcon = TownyProvincesSettings.getTownCostsIcon();
				BufferedImage resizedIcon = new BufferedImage(
					TownyProvincesSettings.getTownCostsIconWidth(),
					TownyProvincesSettings.getTownCostsIconHeight(),
					configIcon.getType()
				);
				Graphics2D g2d = resizedIcon.createGraphics();
				g2d.drawImage(configIcon, 0, 0, TownyProvincesSettings.getTownCostsIconWidth(), TownyProvincesSettings.getTownCostsIconHeight(), null);
				g2d.dispose();
				
				ImageIO.write(resizedIcon, "png", out);
			} catch (IOException ex) {
				TownyProvinces.severe("Failed to put BlueMap Marker Icon as png file!");
				throw new RuntimeException(ex);
			}
		});
		
		TownyProvinces.info("BlueMap support enabled.");
	  }
	  
	@Override
	void executeAction(boolean bordersRefreshRequested, boolean homeBlocksRefreshRequested) {
		BlueMapAPI.getInstance().ifPresent( api -> {
			Optional<BlueMapWorld> world = api.getWorld(TownyProvincesSettings.getWorld());

			if (!world.isPresent()) {
				TownyProvinces.severe("World is not in BlueMap registry!");
				return;
			}

			if (bordersRefreshRequested) {
				if (borderMarkerSet != null) {
					world.get().getMaps().forEach(e -> e.getMarkerSets().remove("townyprovinces.markerset.borders"));
				}
				addProvinceBordersMarkerSet();
			}

			if (homeBlocksRefreshRequested) {
				if (homeBlocksMarkersSet != null) {
					world.get().getMaps().forEach(e -> e.getMarkerSets().remove("townyprovinces.markerset.homeblocks"));
				}
				addProvinceHomeBlocksMarkerSet();
			}
			drawProvinceHomeBlocks();
			drawProvinceBorders();
			}
		);
	}

	private void addProvinceHomeBlocksMarkerSet() {
		BlueMapWorld world = BlueMapAPI.getInstance().get().getWorld(TownyProvincesSettings.getWorld()).get();
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_town_costs").translate(Locale.ROOT);
		homeBlocksMarkersSet = MarkerSet.builder()
			.label(name)
			.defaultHidden(true)
			.build();
		for (BlueMapMap map : world.getMaps()) {
			map.getMarkerSets().put("townyprovinces.markerset.homeblocks", homeBlocksMarkersSet);
		}
	}

	private void addProvinceBordersMarkerSet() {
		BlueMapWorld world = BlueMapAPI.getInstance().get().getWorld(TownyProvincesSettings.getWorld()).get();
		String name = TownyProvinces.getPlugin().getName() + " - " + Translatable.of("dynmap_layer_label_borders").translate(Locale.ROOT);
		borderMarkerSet = MarkerSet.builder()
			.label(name)
			.build();
		for (BlueMapMap map : world.getMaps()) {
			map.getMarkerSets().put("townyprovinces.markerset.borders", borderMarkerSet);
		}
	}

	@Override
	protected void drawProvinceHomeBlocks() {
		boolean biomeCostAdjustmentsEnabled = TownyProvincesSettings.isBiomeCostAdjustmentsEnabled();
		Set<Province> provinceSet = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet());
		
		for(Province province : provinceSet){
			try{
				String iconUrl = "assets/province.png";
				TPCoord homeBlock = province.getHomeBlock();
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				Marker homeBlockMarker = homeBlocksMarkersSet.get(homeBlockMarkerId);
				if(province.getType().canNewTownsBeCreated()){

					if(homeBlockMarker != null) 
						continue;
					int realHomeBlockX = homeBlock.getX() * TownyProvincesSettings.getChunkSideLength();
					int realHomeBlockZ = homeBlock.getZ() * TownyProvincesSettings.getChunkSideLength();

						String markerLabel;
						if (TownyEconomyHandler.isActive()) {
							int newTownCost = (int) (biomeCostAdjustmentsEnabled ? province.getBiomeAdjustedNewTownCost() : province.getNewTownCost());
							String newTownCostString = TownyEconomyHandler.getFormattedBalance(newTownCost);
							int upkeepTownCost = (int) (biomeCostAdjustmentsEnabled ? province.getBiomeAdjustedUpkeepTownCost() : province.getUpkeepTownCost());
							String upkeepTownCostString = TownyEconomyHandler.getFormattedBalance(upkeepTownCost);
							markerLabel = Translatable.of("dynmap_province_homeblock_label", newTownCostString, upkeepTownCostString).translate(Locale.ROOT);
						} else {
							markerLabel = "";
						}

						homeBlockMarker = POIMarker.builder()
							.label(markerLabel)
							.detail(markerLabel)
							.position(realHomeBlockX, 65.0, realHomeBlockZ)
							.icon(iconUrl, 8, 8)
							.build();

						homeBlocksMarkersSet.put(homeBlockMarkerId, homeBlockMarker);

					}else{
						
						if(homeBlockMarker == null)
							continue;
						homeBlocksMarkersSet.remove(homeBlockMarkerId);
						return;
					}
			}catch (Exception ex){
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected void setProvinceMapStyles() {
		Marker marker;
		for(Province province : new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())){
			marker = borderMarkerSet.get(province.getId());
			
			Color borderColor = new Color(province.getType().getBorderColour(), (float) province.getType().getBorderOpacity());
			Color fillColor = new Color(province.getFillColour(), (float) province.getFillOpacity());
			
			if (marker == null) {
				TownyProvinces.severe("ERROR: Province marker for styling is null");
				continue;
			}
			
			if (marker instanceof ShapeMarker) {
				ShapeMarker shapeMarker = (ShapeMarker) marker;
				shapeMarker.setLineColor(borderColor);
				shapeMarker.setFillColor(fillColor);
			}
		}
	}

	@Override
	protected void drawProvinceBorder(Province province) {
		Color borderColor = new Color(province.getType().getBorderColour(), (float) province.getType().getBorderOpacity());
		String markerId = province.getId();
		Marker marker = borderMarkerSet.get(markerId);
			if (marker == null) {
				Set<TPCoord> borderCoords = findAllBorderCoords(province);
				if (borderCoords.size() > 0) {
					//Arrange border blocks into drawable line
					List<TPCoord> drawableLineOfBorderCoords = arrangeBorderCoordsIntoDrawableLine(borderCoords);
					
					//Draw line
					if (drawableLineOfBorderCoords.size() > 0) {
						drawBorderLine(drawableLineOfBorderCoords, province, markerId);
					} else {
						TownyProvinces.severe("WARNING: Could not arrange province coords into drawable line. If this message has not stopped repeating a few minutes after your server starts, please report it to TownyAdvanced.");
					}
				}
			}
		if (marker instanceof ShapeMarker) {
			ShapeMarker shapeMarker = (ShapeMarker) marker;
			shapeMarker.setLineColor(borderColor);
		}
	}

	private void drawBorderLine(List<TPCoord> drawableLineOfBorderCoords, Province province, String markerId) {
		Color borderColor = new Color(province.getType().getBorderColour(), (float) province.getType().getBorderOpacity());
		Color fillColor = new Color(province.getFillColour(), (float) province.getFillOpacity());
		List<Vector2d> points = new ArrayList<>();
		for(TPCoord drawableLineOfBorderCoord : drawableLineOfBorderCoords){
			int x = (drawableLineOfBorderCoord.getX() * TownyProvincesSettings.getChunkSideLength());
			int z = (drawableLineOfBorderCoord.getZ() * TownyProvincesSettings.getChunkSideLength());
			
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
			
			points.add(Vector2d.from(x,z));
		}
		Shape shape = new Shape(points);
		ShapeMarker marker = ShapeMarker.builder()
			.shape(shape, 65)
			.fillColor(fillColor)
			.lineColor(borderColor)
			.label(province.getId())
			.depthTestEnabled(false)
			.build();
		borderMarkerSet.put(markerId, marker);
	}
}
