package io.github.townyadvanced.townyprovinces.province_generation_job;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import io.github.townyadvanced.townyprovinces.util.TownyProvincesMathUtil;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RegionRegenerationJob extends BukkitRunnable {
	
	public static RegionRegenerationJob regionRegenerationJob = null;
	
	public static boolean startJob(String regionName) {
		if(regionRegenerationJob != null) {
			TownyProvinces.severe("Job In Progress. You must wait until the current job ends until you start a new one.");
			return false;
		} else {
			regionRegenerationJob = new RegionRegenerationJob(regionName);
			regionRegenerationJob.runTaskAsynchronously(TownyProvinces.getPlugin());
			return true;
		}
	}

	/**
	 * Region name for this job
	 */
	private final String regionName;
	/**
	 * Map of currently unclaimed coords
	 * When this is filled,
	 * it essentially becomes the "queue" for the job.
	 * 
	 * Anything which the job does not claim, will later be implicitly considered to be a border
	 * 
	 * NOTE: Both the key and value are always the same object
	 * The allows us to easily retrieve the original coord object,
	 * something which would be difficult if this was a set
	 **/
	private Map<TPCoord, TPCoord> unclaimedCoordsMap;
	private final int regionMinX;
	private final int regionMaxX;
	private final int regionMinZ;
	private final int regionMaxZ;
	private final int mapMinX;
	private final int mapMaxX;
	private final int mapMinZ;
	private final int mapMaxZ;
	private final int minBrushMoveAmount;
	private final int maxBrushMoveAmount;
	private final int brushSquareRadius;
	private final int claimAreaLimitInSquareMetres;
	public final static double CHUNK_AREA_IN_SQUARE_METRES = Math.pow(TownyProvincesSettings.getProvinceBlockSideLength(), 2);
	public final TPCoord searchCoord;
	
	public RegionRegenerationJob(String regionName) {
		this.regionName = regionName;
		//Create region definitions folder and sample files if needed
		if(!FileUtil.createRegionDefinitionsFolderAndSampleFiles()) {
			throw new RuntimeException("Problem creation region definitions folder and sample files");
		}
		//Reload region definitions
		if(!TownyProvincesSettings.loadRegionDefinitions()) {
			throw new RuntimeException("Problem reloading region definitions");
		}
		//this.allCoordsOnMap = TownyProvincesDataHolder.getInstance().getAllCoordsOnMap();
		this.unclaimedCoordsMap = null;
		this.regionMinX = (TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		this.regionMaxX  = (TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		this.regionMinZ = (TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		this.regionMaxZ  = (TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		this.minBrushMoveAmount = TownyProvincesSettings.getProvinceCreatorBrushMinMoveInChunks(regionName);
		this.maxBrushMoveAmount= TownyProvincesSettings.getProvinceCreatorBrushMaxMoveInChunks(regionName);
		this.brushSquareRadius = TownyProvincesSettings.getProvinceCreatorBrushSquareRadiusInChunks(regionName);
		this.claimAreaLimitInSquareMetres = TownyProvincesSettings.getProvinceCreatorBrushClaimLimitInSquareMetres(regionName);
		String nameOfFirstRegion = TownyProvincesSettings.getNameOfFirstRegion();
		this.mapMinX = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		this.mapMaxX  = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		this.mapMinZ = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		this.mapMaxZ  = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		this.searchCoord = new TPCoord(0,0);
	}
	
	@Override
	public void run() {
		TownyProvinces.info("Region regeneration Job Started");
		//Create region definitions folder and sample files if needed
		if(!FileUtil.createRegionDefinitionsFolderAndSampleFiles()) {
			throw new RuntimeException("Problem creation region definitions folder and sample files");
		}
		//Reload region definitions
		if(!TownyProvincesSettings.loadRegionDefinitions()) {
			throw new RuntimeException("Problem reloading region definitions");
		}
		//Paint region(s)
		boolean paintingSuccess;
		if(regionName.equalsIgnoreCase("ALL")) {
			//Create a new local map of soon-to-be-unclaimed coords
			Map<TPCoord, TPCoord> soonToBeUnclaimedCoords = TownyProvincesDataHolder.getInstance().getAllCoordsOnMap();
			//Clear the data maps 
			TownyProvincesDataHolder.getInstance().getProvincesSet().clear();
			TownyProvincesDataHolder.getInstance().getCoordProvinceMap().clear();
			//Assign the map of unclaimed coords
			unclaimedCoordsMap = soonToBeUnclaimedCoords;
			//Paint all regions
			paintingSuccess = paintAllRegions();
		} else {
			//Clear provinces which are mostly in the given area
			deleteExistingProvincesWhichAreMostlyInSpecifiedArea(regionName);
			//Create and assign the map of unclaimed coords
			unclaimedCoordsMap = TownyProvincesDataHolder.getInstance().getAllUnclaimedCoordsInRegion(regionName);
			//Paint one region
			RegionPaintTask regionPaintTask = new RegionPaintTask(regionName, unclaimedCoordsMap);
			paintingSuccess = regionPaintTask.executeTask();
		}
		if(!paintingSuccess) {
			TownyProvinces.info("Problem Painting Regions");
			return;
		}
		//Allocate unclaimed chunks to provinces.
		if(!assignUnclaimedCoordsToProvinces()) {
			TownyProvinces.info("Problem assigning unclaimed chunks to provinces");
			return;
		}
		//Delete empty provinces
		if(!deleteEmptyProvinces()) {
			TownyProvinces.info("Problem deleting empty provinces");
			return;
		}
		//Save data and ask for full dynmap refresh
		DataHandlerUtil.saveAllData();
		TownyProvinces.getPlugin().getDynmapIntegration().requestFullMapRefresh();
		//Messaging
		if(regionName.equalsIgnoreCase("ALL")) {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_all_regions").translate(Locale.ROOT));
		} else {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_one_regions", regionName).translate(Locale.ROOT));
		}
		//Job Complete
		regionRegenerationJob = null;
		TownyProvinces.info("Region regeneration Job Complete"); //TODO - maybe global message?
	}
	
	public boolean paintAllRegions() {
		//Paint all Regions
		List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
		Collections.sort(regionNames); //Sort in alphabetical order
		for (String regionName: regionNames) {
			RegionPaintTask regionPaintTask = new RegionPaintTask(regionName, unclaimedCoordsMap);
			if(!regionPaintTask.executeTask()) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean deleteEmptyProvinces() {
		TownyProvinces.info("Now Deleting Empty Provinces.");
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			if(province.getCoordsInProvince().size() == 0) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
			}
		}
		TownyProvinces.info("Empty Provinces Deleted.");
		return true;
	}

	private static boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea(String regionName) {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			List<TPCoord> coordsInProvince = province.getCoordsInProvince();
			int numProvinceBlocksInSpecifiedArea = 0;
			for (TPCoord coordInProvince : coordsInProvince) {
				if (coordInProvince.getX() < minX)
					continue;
				else if (coordInProvince.getX() > maxX)
					continue;
				else if (coordInProvince.getZ() < minZ)
					continue;
				else if (coordInProvince.getZ() > maxZ)
					continue;
				numProvinceBlocksInSpecifiedArea++;
			}
			if(numProvinceBlocksInSpecifiedArea > (coordsInProvince.size() / 2)) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
				numProvincesDeleted++;
			}
		}
		TownyProvinces.info("" + numProvincesDeleted + " provinces deleted.");
		return true;
	}

	/**
	 * Assign unclaimed coords to provinces, until you can assign no more
	 */
	private boolean assignUnclaimedCoordsToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		Map<TPCoord, Province> pendingCoordProvinceAssignments = new HashMap<>();
		while(true) {
			//Rebuild the map of pending coord-province assignments
			rebuildPendingCoordProvinceAssignmentMap(unclaimedCoordsMap, pendingCoordProvinceAssignments);
			TownyProvinces.info("Num Unclaimed Chunks: " + unclaimedCoordsMap.size());
			TownyProvinces.info("Pending Chunk-Province Assignments: " + pendingCoordProvinceAssignments.size());
			
			//Exit loop if there are no more pending assignments
			if(pendingCoordProvinceAssignments.size() == 0) {
				break;
			}
			/*
			 * Do all the pending assignments
			 * Except those which are no longer valid when you get to them
			 */
			for(Map.Entry<TPCoord,Province> mapEntry: pendingCoordProvinceAssignments.entrySet()) {
				if(verifyCoordEligibilityForProvinceAssignment(mapEntry.getKey())) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(mapEntry.getKey(), mapEntry.getValue());
					unclaimedCoordsMap.remove(mapEntry.getKey());
				}
			}
		}
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}
	
	/**
	 * Some coords may now be eligible. Some may be ineligible. Rebuild
	 * 
	 * @param allUnclaimedCoords all unclaimed coords on map
	 */
	private void rebuildPendingCoordProvinceAssignmentMap(Map<TPCoord, TPCoord> allUnclaimedCoords, Map<TPCoord, Province> pendingCoordProvinceAssignmentMap) {
		//Clear map
		pendingCoordProvinceAssignmentMap.clear();
		//Rebuild map
		for (TPCoord unclaimedCoord : allUnclaimedCoords.values()) {
			Province province = getProvinceIfCoordIsEligibleForProvinceAssignment(unclaimedCoord);
			if(province != null) {
				pendingCoordProvinceAssignmentMap.put(unclaimedCoord, province);
			}
		}
	}
	
	private boolean verifyCoordEligibilityForProvinceAssignment(TPCoord coord) {
		Province province = getProvinceIfCoordIsEligibleForProvinceAssignment(coord);
		return province != null;
	}

	private Province getProvinceIfCoordIsEligibleForProvinceAssignment(TPCoord candidateCoord) {
		//Filter out chunk if it is at edge of map
		if(candidateCoord.getX() < mapMinZ)
			return null;
		else if (candidateCoord.getX() > mapMaxX)
			return null;
		else if (candidateCoord.getZ() < mapMinZ)
			return null;
		else if (candidateCoord.getZ() > mapMaxZ)
			return null;

		//Check cardinal direction
		Province province;
		Province result = null;
		int[] x = new int[]{0,-1,1,0};
		int[] z = new int[]{-1,0,0,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(x[i], z[i]);
			if (province != null) {
				if(result == null) {
					result = province;
				} else {
					return null; //Can't have 2 adjacent provinces
				}
			}
		}

		if(result == null)
			return null; //No province found cardinally
		
		//Check non-cardinal
		x = new int[]{-1,1,1,-1};
		z = new int[]{-1,-1,1,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(x[i], z[i]);
			if (province != null) {
				return null; //A winner can't have any adjacent in non-cardinal
			}
		}
		return result;
	}
	
	public static Set<Coord> findAllAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			result.add(new Coord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
		}
		return result;
	}






}

 