package io.github.townyadvanced.townyprovinces.integrations.dynmap;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.ProvinceGeneratorUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynmapIntegration {
	
	private final MarkerAPI markerapi;
	private BukkitTask dynmapTask;
	private MarkerSet markerSet;

	public DynmapIntegration() {
		DynmapAPI dynmapAPI = (DynmapAPI) TownyProvinces.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
		markerapi = dynmapAPI.getMarkerAPI();
		addMarkerSet();
		//registerDynmapTownyListener();
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
		TownyProvinces.info("Dynmap Task Starting");
		dynmapTask = new DynmapTask(this).runTaskTimerAsynchronously(TownyProvinces.getPlugin(), 40, 300);
		TownyProvinces.info("Dynmap Task Started");
	}

	public void endDynmapTask() {
		dynmapTask.cancel();
	}

	/**
	 * Display all TownyProvinces items
	 */
	void displayTownyProvinces() {
		//debugDrawProvinceHomeBlocks();
		drawProvinceBorders();
	}

	/**
	 * Method only used for debug. Draws the province homeblocks as fire icons
	 */
	private void debugDrawProvinceHomeBlocks() {
		String FIRE_ICON = "fire";
		for (Province province : TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			try {
				Coord homeBlock = province.getHomeBlock();
				int realHomeBlockX = homeBlock.getX() * TownyProvincesSettings.getProvinceBlockSideLength();
				int realHomeBlockZ = homeBlock.getZ() * TownyProvincesSettings.getProvinceBlockSideLength();

				MarkerIcon homeBlockIcon = markerapi.getMarkerIcon(FIRE_ICON);
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

	private void drawProvinceBorders() {
		//Find and draw the borders around each province
		for (Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			try {
				String markerId = "border_of_province_" + province.getUuid().toString();
				drawProvinceBorder(province, markerId);
			} catch (Throwable t) {
				TownyProvinces.severe("Could not draw province borders for province " + province.getUuid());
				t.printStackTrace();
			}
		}
	}
	
	private void drawProvinceBorder(Province province, String markerId) {
		PolyLineMarker polyLineMarker = markerSet.findPolyLineMarker(markerId);
		if(polyLineMarker == null) {
			//Get border blocks
			Set<ProvinceBlock> borderBlocks = findAllBorderBlocks(province);
			if(borderBlocks.size() > 0) {
				//Arrange border blocks into drawable line
				List<ProvinceBlock> drawableLineOfBorderBlocks = arrangeBorderBlocksIntoDrawableLine(borderBlocks);
				//Draw line
				drawBorderLine(drawableLineOfBorderBlocks, province, markerId);
			}
		} else {
			//Re-evaluate colour 
			if (province.isDeleted()) {
				if (polyLineMarker.getLineColor() != TownyProvincesSettings.getDeletedBorderColour()) {
					//Change colour of marker
					polyLineMarker.setLineStyle(TownyProvincesSettings.getDeletedBorderWeight(), TownyProvincesSettings.getDeletedBorderOpacity(), TownyProvincesSettings.getDeletedBorderColour());
				}
			} else {
				if (polyLineMarker.getLineColor() != TownyProvincesSettings.getActiveBorderColour()) {
					//Change colour of marker
					polyLineMarker.setLineStyle(TownyProvincesSettings.getActiveBorderWeight(), TownyProvincesSettings.getActiveBorderOpacity(), TownyProvincesSettings.getActiveBorderColour());
				}
			}
		} 
	}
	
	private List<ProvinceBlock> arrangeBorderBlocksIntoDrawableLine(Set<ProvinceBlock> unsortedBorderBlocks) {
		List<ProvinceBlock> result = new ArrayList<>();
		List<ProvinceBlock> unProcessedBlocks = new ArrayList<>(unsortedBorderBlocks);
		ProvinceBlock lineHead = (new ArrayList<>(unProcessedBlocks)).get(0);
		ProvinceBlock candidate;
		while(unProcessedBlocks.size() > 0) {
			candidate = unProcessedBlocks.get((int)(Math.random() * unProcessedBlocks.size()));
			if(areCoordsCardinallyAdjacent(candidate.getCoord(), lineHead.getCoord())) {
				lineHead = candidate;
				result.add(candidate);
				unProcessedBlocks.remove(candidate);
			}
		}
		//Add last block to line, to make a circuit
		result.add(result.get(0));
		//Return result
		return result;
	}

	/**
	 * Find the border coords around the given province
	 * 
	 * Note that these co-cords will not actually belong to the province
	 * Rather they will all have provinceBorder=true, and province=null
	 */
	public static Set<ProvinceBlock> findAllBorderBlocks(Province province) {
		Set<ProvinceBlock> resultSet = new HashSet<>();
		for(ProvinceBlock borderBlock: TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
			if(isBorderBlockAdjacentToProvince(borderBlock, province)) {
				resultSet.add(borderBlock);
			}
		}
		return resultSet;
	}

	private static boolean isBorderBlockAdjacentToProvince(ProvinceBlock givenBorderBlock, Province givenProvince) {
		Set<Coord> allAdjacentCoords = ProvinceGeneratorUtil.findAllAdjacentCoords(givenBorderBlock.getCoord());
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
	
	private boolean areCoordsCardinallyAdjacent(Coord c1, Coord c2) {
		return MathUtil.distance(c1,c2) == 1;
	}

	private void drawBorderLine(List<ProvinceBlock> drawableListOfBorderBlocks, Province province, String markerId) {
		String worldName = TownyProvincesSettings.getWorldName();
		int borderColour = TownyProvincesSettings.getActiveBorderColour();
		int borderWeight = TownyProvincesSettings.getActiveBorderWeight();
		double borderOpacity = TownyProvincesSettings.getActiveBorderOpacity();
		int deletedBorderColour = TownyProvincesSettings.getDeletedBorderColour();
		int deletedBorderWeight = TownyProvincesSettings.getDeletedBorderWeight();
		double deletedBorderOpacity = TownyProvincesSettings.getDeletedBorderOpacity();

		double[] xPoints = new double[drawableListOfBorderBlocks.size()];
		double[] zPoints = new double[drawableListOfBorderBlocks.size()];
		for (int i = 0; i < drawableListOfBorderBlocks.size(); i++) {
			xPoints[i] = (drawableListOfBorderBlocks.get(i).getCoord().getX() * TownyProvincesSettings.getProvinceBlockSideLength());
			zPoints[i] = (drawableListOfBorderBlocks.get(i).getCoord().getZ() * TownyProvincesSettings.getProvinceBlockSideLength());

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
			Coord pullStrengthFromNearbyProvince = calculatePullStrengthFromNearbyProvince(drawableListOfBorderBlocks.get(i).getCoord(), province);
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
		PolyLineMarker polyLineMarker = markerSet.createPolyLineMarker(
			markerId, markerName, unknown, worldName,
			xPoints, zPoints, zPoints, unknown2);
		
		//Set colour
		if (province.isDeleted()) {
			polyLineMarker.setLineStyle(deletedBorderWeight, deletedBorderOpacity, deletedBorderColour);
		} else {
			polyLineMarker.setLineStyle(borderWeight, borderOpacity, borderColour);
		}
	}

	
	
	private Coord calculatePullStrengthFromNearbyProvince(Coord borderCoordBeingPulled, Province provinceDoingThePulling) {
		int pullStrengthX = 0;
		int pullStrengthZ = 0;
		Set<Coord> adjacentCoords = ProvinceGeneratorUtil.findAllAdjacentCoords(borderCoordBeingPulled);
		ProvinceBlock adjacentProvinceBlock;
		for(Coord adjacentCoord: adjacentCoords) {
			adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(adjacentProvinceBlock != null && !adjacentProvinceBlock.isProvinceBorder() && adjacentProvinceBlock.getProvince().equals(provinceDoingThePulling)) {
				pullStrengthX += (adjacentCoord.getX() - borderCoordBeingPulled.getX());
				pullStrengthZ += (adjacentCoord.getZ() - borderCoordBeingPulled.getZ());
			}
		}
		return new Coord(pullStrengthX, pullStrengthZ);
	}
	
	////////////////////////// DEBUG SECTION ////////////////////////
	
	//Shows all borders. But not for production
	private void debugDrawProvinceBorders() {
		String worldName = TownyProvincesSettings.getWorldName();

		for (ProvinceBlock provinceBlock : TownyProvincesDataHolder.getInstance().getProvinceBorderBlocks()) {
			debugDrawProvinceBorderBlock(worldName, provinceBlock);
		}
	}
	
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
}
