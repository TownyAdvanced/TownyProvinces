package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.CoordLine;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.ProvinceCreatorUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DynmapIntegration {

	private static final String TEMP_ICON = "fire";
	private final MarkerAPI markerapi;
	private BukkitTask dynmapTask;
	private final Map<UUID, Marker> townUUIDToSiegeMarkerMap = new HashMap<>();
	private MarkerSet markerSet;

	public DynmapIntegration() {
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
		markerapi = dynmapAPI.getMarkerAPI();
		addMarkerSet();
		registerDynmapTownyListener();
		startDynmapTask();
		TownyProvinces.info("Dynmap support enabled.");
	}

	private void addMarkerSet() {
		TownyProvinces plugin = TownyProvinces.getPlugin();

		if (markerapi == null) {
			TownyProvinces.severe("Error loading Dynmap marker API!");
			return;
		}

		//Create marker set
		markerSet = markerapi.getMarkerSet("townyprovinces.markerset");
		if (markerSet == null) {
			markerSet = markerapi.createMarkerSet("townyprovinces.markerset", plugin.getName(), null, false);
		} else {
			markerSet.setMarkerSetLabel(plugin.getName());
		}

		if (markerSet == null) {
			TownyProvinces.severe("Error creating Dynmap marker set!");
			return;
		}

		//Create battle banner marker icon
		//markerapi.createMarkerIcon(BATTLE_BANNER_ICON_ID, "BattleBanner", plugin.getResource(Settings.BATTLE_BANNER_FILE_NAME));
	}

	private void registerDynmapTownyListener() {
		TownyProvinces plugin = TownyProvinces.getPlugin();
		PluginManager pm = plugin.getServer().getPluginManager();
		if (pm.isPluginEnabled("Dynmap-Towny")) {
			TownyProvinces.info("TownyProvinces found Dynmap-Towny plugin, enabling Dynmap-Towny support.");
			pm.registerEvents(new DynmapTownyListener(), plugin);
		} else {
			TownyProvinces.info("Dynmap-Towny plugin not found.");
		}
	}

	public void startDynmapTask() {
		dynmapTask = new DynmapTask(this).runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
	}

	public void endDynmapTask() {
		dynmapTask.cancel();
	}

	/**
	 * Display all TownyProvinces items, including province boundaries, region boundaries etc.
	 */
	void displayTownyProvinces() {
		//Map<UUID, Marker> townUUIDToSiegeMarkerMapCopy = new HashMap<>(townUUIDToSiegeMarkerMap);

		{
			//Cleanup markers ....... This would involve activating/de-activating roads etc.
			//Region/Province borders probably don't need to be re-done on a task
			/*
            UUID townUUID = null;
            Marker marker = null;
            for (Map.Entry<UUID, Marker> mapEntry : townUUIDToSiegeMarkerMapCopy.entrySet()) {
                try {
                    marker = null;
                    townUUID = null;
                    townUUID = mapEntry.getKey();
                    marker = mapEntry.getValue();
                    Siege siege = SiegeController.getSiegeByTownUUID(townUUID);

                    if (siege == null || siege.getStatus() != SiegeStatus.IN_PROGRESS) {
                        //Delete marker if siege is not in progress
                        marker.deleteMarker();
                        townUUIDToSiegeMarkerMap.remove(townUUID);

                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(TEMP_ICON)) {
                        /*
                         * Change to battle icon if siege is active.
                         */
			/*
                        if(!isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID));                             
                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(BATTLE_BANNER_ICON_ID)) {
                        /*
                         * Change to peaceful icon if siege is dormant
                         */
			
			/*
                        if (isSiegeDormant(siege))
                            marker.setMarkerIcon(markerapi.getMarkerIcon(TEMP_ICON));                      
                    }
                } catch (Exception e) {
                    if (marker != null)
                        marker.deleteMarker();
                    townUUIDToSiegeMarkerMap.remove(townUUID);
                }
            }
        }
	*/
			{

			}

			//Draw all province blocks
			//DRAW ALL PROVINCE BORDER
			//Cycle through each province
			
			//{

/*			
				String worldName = TownyProvincesSettings.getWorldName();
				for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {

					for (ProvinceBlock provinceBlock : findBorderBlocks(province)) {
						if (provinceBlock.isProvinceBorder())
							drawProvinceBorderBlock(worldName, provinceBlock);
					//}
				//}
			//}
			
 */
			
				//drawTestArea();
			
			{
				//drawProvinceBorders();

				//for()

				//drawProvinceBorders2();
				
				////Cycle through each province
				//for (ProvinceBlock provinceBlock : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
				//	drawProvinceBorderBlock(TownyProvincesSettings.getWorldName(), provinceBlock);
			//	}
			}
			
			debugDrawProvinceHomeBlocks();
			
			//debugDrawProvinceBorders();
			
			drawProvinceBorders4();

		}
	}
	
	
	private void debugDrawProvinceHomeBlocks() {
		//TEMP - Add markers showing province homeblocks
		for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {
			try {
				Coord homeBlock = province.getHomeBlock();
				int realHomeBlockX = homeBlock.getX() * TownyProvincesSettings.getProvinceBlockSideLength();
				int realHomeBlockZ = homeBlock.getZ() * TownyProvincesSettings.getProvinceBlockSideLength();

				MarkerIcon homeBlockIcon = markerapi.getMarkerIcon(TEMP_ICON);
				String homeBlockMarkerId = "province_homeblock_" + homeBlock.getX() + "-" + homeBlock.getZ();
				String name = homeBlockMarkerId;
				Marker homeBlockMarker = markerSet.findMarker(homeBlockMarkerId);
				if (homeBlockMarker == null) {
					homeBlockMarker = markerSet.createMarker(
						homeBlockMarkerId, name, TownyProvincesSettings.getWorldName(),
						realHomeBlockX, 64, realHomeBlockZ,
						homeBlockIcon, false);
					homeBlockMarker.setLabel(name);
					homeBlockMarker.setDescription("test description");
				}
			} catch (Exception ex) {
				TownyProvinces.severe("Problem adding homeblock marker");
				ex.printStackTrace();
			}
		}
	}
	
	//Shows all borders. But not for production
	private void debugDrawProvinceBorders() {
		String worldName = TownyProvincesSettings.getWorldName();
		
		for (ProvinceBlock provinceBlock : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
			debugDrawProvinceBorderBlock(worldName, provinceBlock);
		}
	}


	private void drawProvinceBorders4() {
		//Find and draw the borders around each province
		for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {

			//Get border blocks
			Set<ProvinceBlock> borderBlocks = findAllBorderBlocks(province);

			//DEBUG DRAW
			String worldName = TownyProvincesSettings.getWorldName();
			for (ProvinceBlock borderBlock : borderBlocks) {
				debugDrawProvinceBorderBlock(worldName, borderBlock);
			}
			
			//List<Coord> drawableProvinceBordersCoords = generateDrawableProvinceBorderCoords(provinceBorderCoords);
			//drawProvinceBorders(province, drawableProvinceBordersCoords);
		}
	}
	
	

	private void drawProvinceBorders3() {
		for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {
			//Get border coords
			//Set<Coord> coords = findAllBorderCoords(province);
			//Validate that they are already marker as border
			//for(Coord)
			
			//DEBUG
			//String worldName = TownyProvincesSettings.getWorldName();
			//for (ProvinceBlock provinceBlock : provinceBorderCoords) {
			//	drawProvinceBorderBlock(worldName, provinceBlock);
			//}
			
			//List<Coord> drawableProvinceBordersCoords = generateDrawableProvinceBorderCoords(provinceBorderCoords);
			//drawProvinceBorders(province, drawableProvinceBordersCoords);
		}			
	}

	/**
	 * Arrange the coords into a drawable line
	 *
	 * @param unsortedProvinceBorderCoords the border coords of the province, unsorted
	 * @return coords as a drawable line
	 */
	private List<Coord> generateDrawableProvinceBorderCoords(Set<Coord> unsortedProvinceBorderCoords) {
		List<Coord> result = new ArrayList<>();
		List<Coord> queue = new ArrayList<>(unsortedProvinceBorderCoords);
		Coord lineHead = null;
		
		while(queue.size() > 0) {
			lineHead = findNextLineHead(queue, lineHead, result);
			queue.remove(lineHead);
			result.add(lineHead);
		}
		
		//Now we have come full circle around the province. Add the last coord
		result.add(result.get(0));
		
		return result;
	}


	/**
	 * Find the border coords around the given province
	 * 
	 * Note that these co-cords will not actually belong to the province
	 * Rather they will all have provinceBorder=true, and province=null
	 */
	private Set<ProvinceBlock> findAllBorderBlocks(Province province) {
		Set<ProvinceBlock> resultSet = new HashSet<>();
		for(ProvinceBlock borderBlock: TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
			if(doesBorderBlockBorderProvince(borderBlock, province)) {
				resultSet.add(borderBlock);
			}
		}
		return resultSet;
	}

	private boolean doesBorderBlockBorderProvince(ProvinceBlock givenBorderBlock, Province givenProvince) {
		Set<Coord> allAdjacentCoords = ProvinceCreatorUtil.findAllAdjacentCoords(givenBorderBlock.getCoord());
		ProvinceBlock candidateProvinceBlock;
		for(Coord candidateCoord: allAdjacentCoords) {
			candidateProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(candidateCoord);
			if(candidateProvinceBlock != null 
					&& !candidateProvinceBlock.isProvinceBorder()
					&& candidateProvinceBlock.getProvince().equals(givenProvince)) {
				return true;  //The given block borders the given province	
			}
		}
		return false; //The given block does not border the given province
	}


	private boolean isProvinceBlockOnBorder(ProvinceBlock givenProvinceBlock) {
		Set<Coord> allAdjacentCoords = ProvinceCreatorUtil.findAllAdjacentCoords(givenProvinceBlock.getCoord());
		ProvinceBlock candidateProvinceBlock;
		for(Coord candidateCoord: allAdjacentCoords) {
			candidateProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(candidateCoord);
			if(candidateProvinceBlock == null) {
				//Is on edge of map
				return true;
			} else if (!candidateProvinceBlock.getProvince().equals(givenProvinceBlock.getProvince())) {
				//Borders another province
				return true;
			}
		}
		//No adjacent foreign province found
		return false;
	}

	private Set<Coord> findAllBorderCoords(ProvinceBlock givenProvinceBlock) {
		Set<Coord> result = new HashSet<>();
		Set<Coord> allAdjacentCoords = ProvinceCreatorUtil.findAllAdjacentCoords(givenProvinceBlock.getCoord());
		ProvinceBlock candidateProvinceBlock;
		for(Coord candidateCoord: allAdjacentCoords) {
			candidateProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(candidateCoord);
			if(candidateProvinceBlock == null) {
				//edge of map
				result.add(candidateCoord);
			} else if (!candidateProvinceBlock.getProvince().equals(givenProvinceBlock.getProvince())) {
				//Borders another province
				result.add(candidateCoord);
			}
		}
		return result;
	}

	private Set<Coord> findAllAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		for(int x = -1; x <= targetCoord.getX() + 1; x++) {
			for(int z = -1; z <= targetCoord.getZ() + 1; z++) {
				if(x != 0 && z != 0) {
					result.add(new Coord(x,z));
				}
			}
		}
		return result;
	}


	private void drawTestArea() {
		double[] xPoints = new double[5];
		xPoints[0] = 0;
		xPoints[1] = 100;
		xPoints[2] = 100;
		xPoints[3] = 0;
		xPoints[4] = 0;
		//xPoints[5] = 500;
		//xPoints[6] = 600;
		//xPoints[7] = 600;
		//xPoints[8] = 500;
		//xPoints[9] = 500;

		double[] zPoints = new double[5];
		zPoints[0] = 0;
		zPoints[1] = 0;
		zPoints[2] = 100;
		zPoints[3] = 100;
		zPoints[4] = 0;

		//zPoints[5] = 500;
		//zPoints[6] = 500;
		//zPoints[7] = 600;
		//zPoints[8] = 600;
		//zPoints[9] = 500;

		String worldName = TownyProvincesSettings.getWorldName();
		String markerId = "tEST mARKER";
		String markerName = "tEST MARKER";
		//markerName += " Is Border: " + provinceBlock.isProvinceBorder();

		boolean unknown = false;
		boolean unknown2 = false;

		//AreaMarker areaMarker = markerSet.createAreaMarker(
	//		markerId, markerName, unknown, worldName,
	//		xPoints, zPoints, unknown2);

		PolyLineMarker polyLineMarker =  markerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);

polyLineMarker.setLineStyle(8,0.4, 300000);
//polyLineMarker.set
		//areaMarker.setFillStyle(0.2, 500000);
	}
	
	public void drawProvinceBorders() {
		Set<CoordLine> borderLines = calculateBorderLinesForDrawing();
		TownyProvinces.info("Num or border lines: " + borderLines.size());
		drawBorderLines(borderLines);
		
		//for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
		//	drawProvinceBorders(province);
		//}
	}

	private void drawBorderLines(Set<CoordLine> borderLines) {
		//For now lets be simple and draw each line as a poly line
		for(CoordLine borderLine: borderLines) {
			
			
			TownyProvinces.info("Border Line Size: " + borderLine.getPoints().size());
			String worldName = TownyProvincesSettings.getWorldName();
			List<Coord> borderCoords = borderLine.getPoints();

			double[] xPoints = new double[borderCoords.size()];
			double[] yPoints = new double[borderCoords.size()];
			double[] zPoints = new double[borderCoords.size()];

			Coord coord;
			for (int i = 0; i < borderCoords.size(); i++) {
				coord = borderCoords.get(i);
				xPoints[i] = coord.getX() * TownyProvincesSettings.getProvinceBlockSideLength();
				yPoints[i] = 64;
				zPoints[i] = coord.getZ() * TownyProvincesSettings.getProvinceBlockSideLength();;
			}

			String markerId = "Border_Line" 
				+ "_x" + xPoints[0] + "_z" + zPoints[0] 
				+ "_x" + xPoints[xPoints.length-1] + "_z" + zPoints[zPoints.length-1];
			String markerName = "Border Line";
			boolean unknown = false;
			boolean unknown2 = false;

			PolyLineMarker polyLineMarker = markerSet.createPolyLineMarker(
				markerId, markerName, unknown, worldName,
				xPoints, yPoints, zPoints, unknown2);

			polyLineMarker.setLineStyle(4, 0.3, 300000);
		}
		
		
	}

	private Set<CoordLine> calculateBorderLinesForDrawing() {
		
		//First arrange the border coords into lines
		Set<Coord> allBorderCoords = TownyProvincesDataHolder.getInstance().getProvinceBorderCoords();
		Set<Coord> usedBorderCoords = new HashSet<>();
		List<CoordLine> borderLineQueue = new ArrayList<>();
		CoordLine activeBorderLine = null;
		Set<CoordLine> borderLinesReadyForDrawing = new HashSet<>();
		Coord starterCoord ;
		CoordLine newBorderLine ;
		
		//boolean allBordersFound = false; //TODO - use this if you get trouble with the while loop
		//TODO - need to create one line here.....
		
		//Grow all lines until they cannot grow any more
		while(usedBorderCoords.size() < allBorderCoords.size()) {
			
			//Grow a single line
			
			if(activeBorderLine == null) {
				//Pick the next one off the queue
				if(borderLineQueue.size() > 0) {
					activeBorderLine = borderLineQueue.get(0);
					borderLineQueue.remove(0);
					continue;
				} else {
					//Queue is empty. Start a random new line
					Set<Coord> unusedBorderCoords = new HashSet<>(allBorderCoords);
					unusedBorderCoords.removeAll(usedBorderCoords);
					starterCoord = (new ArrayList<>(unusedBorderCoords)).get(0);
					newBorderLine = new CoordLine(starterCoord);
					usedBorderCoords.add(starterCoord);
					activeBorderLine = newBorderLine;
					continue;
				}
			}

			//TownyProvinces.info("Growing line now");
			//A border line is active. Grow it now
			
			List<Coord> borderCoordsInFrontOfLine = calculateCoordsInFrontOfLine(activeBorderLine, allBorderCoords);
				
			if(borderCoordsInFrontOfLine.size() == 0) {
				//End of the line
				borderLinesReadyForDrawing.add(activeBorderLine);
				activeBorderLine = null;
				//TownyProvinces.info("End of the line");
			} else {

				//Queue other lines if required
				for(int i = 1; i < borderCoordsInFrontOfLine.size(); i++) {
					Coord targetPoint = borderCoordsInFrontOfLine.get(i);
					newBorderLine = new CoordLine(activeBorderLine.getLineHead(), targetPoint);
					if (usedBorderCoords.contains(targetPoint)) {
						//Point is already used by another line
						//This means we just met another line. This line is ready for use
						borderLinesReadyForDrawing.add(newBorderLine);
					} else {
						//Queue the new line
						borderLineQueue.add(newBorderLine);
						//Register point as used
						usedBorderCoords.add(targetPoint);
					}
				}
				//TownyProvinces.info("Now growing line itself");
				//TownyProvinces.info("Line head: " + activeBorderLine.getLineHead());
				//TownyProvinces.info("Coord ahead: " + borderCoordsInFrontOfLine.get(0));


				//Grow the line itself
				Coord targetPoint = borderCoordsInFrontOfLine.get(0);
				activeBorderLine.setLineHead(targetPoint);
				activeBorderLine.addPoint(targetPoint);
				if (usedBorderCoords.contains(targetPoint)) {
					//Point is already used by another line
					//This means we just met another line. Stop drawing
					borderLinesReadyForDrawing.add(activeBorderLine);
					activeBorderLine = null;
				} else {
					//Register point as used
					usedBorderCoords.add(targetPoint);
				}


			}

			TownyProvinces.info("Lines ready for drawing: " + borderLinesReadyForDrawing.size());
		}
		
		//for (Province province : TownyProvincesDataHolder.getInstance().getProvinces()) {
		//	drawProvinceBorders(province);
		//}
		TownyProvinces.info("Lines ready for drawing: " + borderLinesReadyForDrawing.size());
		return borderLinesReadyForDrawing;
	}

	
	public List<Coord> calculateCoordsInFrontOfLine(CoordLine coordLine, Set<Coord> allCoords) {
		//Make candidate set
		Set<Coord> candidates = new HashSet<>(allCoords);
		// Remove from candidates the line head
		candidates.remove(coordLine.getLineHead());
		// Remove from candidates the coord which is just behind the line head (if any)
		if(coordLine.getPoints().size() > 1) {
			candidates.remove(coordLine.getPoints().get(coordLine.getPoints().size() -2));
		}
		// Calculate coords in front of line
		List<Coord> coordsInFrontOfLine = getAdjacentCardinalCoords(coordLine.getLineHead(), candidates);
		return coordsInFrontOfLine;
	}

	private List<Coord> getAdjacentCardinalCoords(Coord target, Set<Coord> candidates) {
		List<Coord> result = new ArrayList<>();
		for(Coord candidate: candidates) {
			if(Math.abs(MathUtil.distance(target, candidate)) == 1) {
				result.add(candidate);
			}
		}
		return result;
	}

	public void drawProvinceBorders(Province province) {
		String worldName = TownyProvincesSettings.getWorldName();
		List<Coord> unsortedBorderCoords = new ArrayList<>(getAdjacentForeignCoords(province));
		List<Coord> borderCoords = arrangeCoordsIntoDrawableLine(unsortedBorderCoords);

		double[] xPoints = new double[borderCoords.size()];
		double[] yPoints = new double[borderCoords.size()];
		double[] zPoints = new double[borderCoords.size()];

		Coord coord;
		for (int i = 0; i < borderCoords.size(); i++) {
			coord = borderCoords.get(i);
			xPoints[i] = coord.getX() * TownyProvincesSettings.getProvinceBlockSideLength();
			yPoints[i] = 64;
			zPoints[i] = coord.getZ() * TownyProvincesSettings.getProvinceBlockSideLength();;
		}

		String markerId = "province border " + province.getHomeBlock().getX() + "_" + province.getHomeBlock().getZ();
		String markerName = "province border name";
		boolean unknown = false;
		boolean unknown2 = false;

		PolyLineMarker polyLineMarker = markerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, yPoints, zPoints, unknown2);

		polyLineMarker.setLineStyle(4, 0.3, 300000);
	}
	
	
	
	public void drawProvinceBorders2() {
		Set<Coord> usedCoords = new HashSet<>();
		
		//Calculate border lines
		Set<CoordLine> borderLines = new HashSet<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			borderLines.addAll(calculateProvinceBorderLines(province, usedCoords));
		}
		TownyProvinces.info("Border Lines Num: " + borderLines.size());
		
		//Draw border lines
		drawBorderLines(borderLines);
	}

	/**
	 * @param province the province
	 * @return used coords
	 */
	private List<CoordLine> calculateProvinceBorderLines(Province province, Set<Coord> usedCoords) {
		
		//Get list of available coords
		Set<Coord> availableCoords = new HashSet<>();
		for(ProvinceBlock provinceBlock: province.getProvinceBlocks()) {
			if(provinceBlock.isProvinceBorder()) {
				availableCoords.add(provinceBlock.getCoord());
			}
		}
		availableCoords.removeAll(usedCoords);
		TownyProvinces.info("Available Coords for province: " + availableCoords.size());
		
		//Now generate lines
		List<CoordLine> result = new ArrayList<>();
		CoordLine activeLine = null;
		while(availableCoords.size() > 0) {
			
			if(activeLine == null) {
				Coord starterCoord = (new ArrayList<>(availableCoords)).get(0);
				activeLine = new CoordLine(starterCoord);
				usedCoords.add(starterCoord);
				availableCoords.remove(starterCoord);
				continue;
			}
			
			//Now we have an active line
			List<Coord> coordsInFrontOfLine = calculateCoordsInFrontOfLine(activeLine, availableCoords);
			if(coordsInFrontOfLine.size() == 0) {
				//End of line
				result.add(activeLine);
				activeLine = null;
			} else {
				//Grow line
				activeLine.setLineHead(coordsInFrontOfLine.get(0));
				activeLine.addPoint(coordsInFrontOfLine.get(0));
				usedCoords.add(coordsInFrontOfLine.get(0));
				availableCoords.remove(coordsInFrontOfLine.get(0));
			}
		}

		TownyProvinces.info("Lines Created for province " + result.size());
		return result;
	}
	
	
	private List<Coord> arrangeCoordsIntoDrawableLine(List<Coord> unorderedCoordsList) {
		List<Coord> queue = new ArrayList<>(unorderedCoordsList);
		List<Coord> result = new ArrayList<>();
		Coord lineHead = null;
		
		while (result.size() < unorderedCoordsList.size()) {
			lineHead = findNextLineHead(queue, lineHead, result);
			queue.remove(lineHead);
			result.add(lineHead);
		}
	
		return result;
	}

	private Coord findNextLineHead(List<Coord> queue, Coord lineHead, List<Coord> line) {
		if(lineHead == null) {
			//Start of line
			return queue.get(0);
		}
		for(Coord candidate: queue) {
			if(areCoordsCardinallyAdjacent(candidate, lineHead)) {
				return candidate;
			}
		}
		throw new RuntimeException("Could not find next line head. Check province borders for errors");
	}
	
		
		
		//	DrawableLineMaker drawableLineMaker = new DrawableLineMaker();
	//	unorderedCoordsList.sort(drawableLineMaker);
	

	private boolean areCoordsCardinallyAdjacent(Coord c1, Coord c2) {
		return MathUtil.distance(c1,c2) == 1;
	}


	private class DrawableLineMaker implements Comparator<Coord>{
		private Coord lineHead;
		private Set<Coord> alreadyDrawn;
		private DrawableLineMaker() {
			lineHead = null;
		}
		@Override
		public int compare(Coord c1, Coord c2) {
			//Create line head if needed
			if(lineHead == null) {
				lineHead = c1;
			}
			//If we are adjacent the line head, return 1 otherwise -1
			if(isAdjacent(c1,lineHead)) {
				//We become the line head
				lineHead = c1;
				return 1;
			} else {
				return -1;
			}
		}
		
		
		private boolean isAdjacent(Coord c1, Coord c2) {
			return MathUtil.distance(c1,c2) < 2;			
		}
	}
	
	/**
	 * Calculate the border coords. These will be just outside the province
	 * This list will need to be ordered later to make a sensible line
	 */
	private static Set<Coord> getAdjacentForeignCoords(Province province) {
		Set<Coord> result = new HashSet<>();
		for (ProvinceBlock provinceBlock : province.getProvinceBlocks()) {
			result.addAll(getAdjacentForeignCoords(provinceBlock));
		}
		TownyProvinces.info("BORDER COORDS FOUND FOR PROVINCE: " + result.size());
		return result;
	}
	
	public static Set<Coord> getAdjacentForeignCoords(ProvinceBlock provinceBlock) {
		Set<Coord> result = new HashSet<>();
		Coord candidateCoord;
		ProvinceBlock candidateProvinceBlock;
		for (int z = -1; z <= 1; z++) {
			for (int x = -1; x <= 1; x++) {
				candidateCoord = new Coord(provinceBlock.getCoord().getX() + x, provinceBlock.getCoord().getZ() + z);
				if(candidateCoord.equals(provinceBlock.getCoord()))
					continue;
				candidateProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(candidateCoord);
				if (candidateProvinceBlock == null) {
					result.add(candidateCoord);   //Not assigned somehow
				} else if (candidateProvinceBlock.getProvince() == null) {
					result.add(candidateCoord);   //A border tile
				}
				
				//I don't think there's any more possibilities here at present
			}
		}
		return result;
	}
		
		
	
	public void debugDrawProvinceBorderBlock(String worldName, ProvinceBlock provinceBlock) {
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
}
