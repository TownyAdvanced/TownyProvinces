package io.github.townyadvanced.townyprovinces.jobs.province_generation;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.dynmap_display.DynmapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RegenerateRegionTask extends BukkitRunnable {
	
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
	private final String regionName;  //This will either be the case correct name of a real region, or "All"
	private final int mapMinXCoord;
	private final int mapMaxXCoord;
	private final int mapMinZCoord;
	private final int mapMaxZCoord;
	public final TPCoord searchCoord;
	
	public RegenerateRegionTask(String regionName) {
		this.regionName = regionName;
		String nameOfFirstRegion = TownyProvincesSettings.getNameOfFirstRegion();
		this.mapMinXCoord = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxXCoord = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		this.mapMinZCoord = TownyProvincesSettings.getTopLeftCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.mapMaxZCoord = TownyProvincesSettings.getBottomRightCornerLocation(nameOfFirstRegion).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		this.searchCoord = new TPFreeCoord(0,0);
	}
	
	@Override
	public void run() {
		try {
			TownyProvinces.info("Regeneration Job Started");
			TownyProvinces.info("Regeneration Job: Acquiring map change lock");
			synchronized (TownyProvinces.MAP_CHANGE_LOCK) {
				TownyProvinces.info("Regeneration Job: Map Change lock acquired");
				executeRegionRegenerationJob();
			}
		} finally {
			TownyProvinces.info("Regeneration Job: Map Change lock released");
			RegenerateRegionTaskController.endTask();
			TownyProvinces.info("Regeneration Job Completed");
		}
	}
	
	public void executeRegionRegenerationJob() {
		//Paint region(s)
		boolean paintingSuccess;
		if(regionName.equalsIgnoreCase("ALL")) {
			//Initialize the unclaimed coords map
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
			//Initialize the unclaimed coords map
			unclaimedCoordsMap = TownyProvincesDataHolder.getInstance().getAllUnclaimedCoordsOnMap();
			//Delete most provinces in the region, except those which are mostly outside
			deleteExistingProvincesWhichAreMostlyInSpecifiedArea(regionName);
			//Paint one region
			paintingSuccess = paintOneRegion(regionName);
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
		//Save data and request full dynmap refresh
		DataHandlerUtil.saveAllData();
		DynmapDisplayTaskController.requestFullMapRefresh();
		//Messaging
		if(regionName.equalsIgnoreCase("ALL")) {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_all_regions").translate(Locale.ROOT));
		} else {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_one_regions", regionName).translate(Locale.ROOT));
		}
		TownyProvinces.info("Region regeneration Job Complete"); //TODO - maybe global message?
	}


	private boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea(String regionName) {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockX() / TownyProvincesSettings.getChunkSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightCornerLocation(regionName).getBlockZ() / TownyProvincesSettings.getChunkSideLength();
		for(Province province: (new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet()))) {
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
				TownyProvincesDataHolder.getInstance().deleteProvince(province, unclaimedCoordsMap);
				numProvincesDeleted++;
			}
		}
		TownyProvinces.info("" + numProvincesDeleted + " provinces deleted.");
		return true;
	}
	
	public boolean paintAllRegions() {
		//Establish order of regions
		List<File> regionDefinitionFiles = FileUtil.readRegionDefinitionFiles();
		Collections.sort(regionDefinitionFiles); //Sort in alphabetical order
		//Paint all Regions
		String regionName;
		boolean firstRegion = true;
		for (File regionDefinitionFile: regionDefinitionFiles) {
			regionName = FileMgmt.loadFileIntoHashMap(regionDefinitionFile).get("region_name");
			if(firstRegion) {
				firstRegion = false;
				//Paint region
				if(!paintOneRegion(regionName)) {
					return false;
				}
			} else {
				//Delete most provinces in the region, except those which are mostly outside
				if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea(regionName)) {
					return false;
				}
				//Paint region
				if(!paintOneRegion(regionName)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean paintOneRegion(String regionName) {
		PaintRegionAction regionPaintTask = new PaintRegionAction(regionName, unclaimedCoordsMap);
		return regionPaintTask.executeAction();
	}
	
	private  boolean deleteEmptyProvinces() {
		TownyProvinces.info("Now Deleting Empty Provinces.");
		Set<Province> provincesToDelete = new HashSet<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.getCoordsInProvince().size() == 0) {
				provincesToDelete.add(province);
			}
		}
		for(Province province: provincesToDelete) {
			TownyProvincesDataHolder.getInstance().deleteProvince(province, unclaimedCoordsMap);
		}
		TownyProvinces.info("Empty Provinces Deleted.");
		return true;
	}

	/**
	 * Assign unclaimed coords to provinces, until you can assign no more
	 */
	private boolean assignUnclaimedCoordsToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		Map<TPCoord, Province> pendingCoordProvinceAssignments = new HashMap<>();
		double totalChunksOnMap = (mapMaxXCoord - mapMinXCoord) * (mapMaxZCoord - mapMinZCoord);
		while(true) {
			//Rebuild the map of pending coord-province assignments
			rebuildPendingCoordProvinceAssignmentMap(pendingCoordProvinceAssignments);
			double totalClaimedChunks = totalChunksOnMap - unclaimedCoordsMap.size(); 
			int percentageChunksClaimed = (int)((totalClaimedChunks / totalChunksOnMap) * 100);
			TownyProvinces.info("Assigning Unclaimed Chunks. Progress: " + percentageChunksClaimed + "%");
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
		TownyProvinces.info("Assigning Unclaimed Chunks. Progress: 100%");
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}
	
	/**
	 * Some coords may now be eligible. Some may be ineligible. Rebuild
	 */
	private void rebuildPendingCoordProvinceAssignmentMap(Map<TPCoord, Province> pendingCoordProvinceAssignmentMap) {
		//Clear map
		pendingCoordProvinceAssignmentMap.clear();
		//Rebuild map
		for (TPCoord unclaimedCoord : unclaimedCoordsMap.values()) {
			Province province = getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(unclaimedCoord);
			if(province != null) {
				pendingCoordProvinceAssignmentMap.put(unclaimedCoord, province);
			}
		}
	}
	
	private boolean verifyCoordEligibilityForProvinceAssignment(TPCoord coord) {
		Province province = getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(coord);
		return province != null;
	}

	/**
	 * Eligibility rules:
	 * 1. At least one claimed chunk must be found cardinally
	 * 2. If any adjacent claimed chunks are found, they must all belong to the same province.
	 * 
	 * @param unclaimedCoord the unclaimed coord
	 * @return the province to assign it to
	 */
	private Province getProvinceIfUnclaimedCoordIsEligibleForProvinceAssignment(TPCoord unclaimedCoord) {
		//Filter out chunk if it is at edge of map
		if(unclaimedCoord.getX() < mapMinXCoord)
			return null;
		else if (unclaimedCoord.getX() > mapMaxXCoord)
			return null;
		else if (unclaimedCoord.getZ() < mapMinZCoord)
			return null;
		else if (unclaimedCoord.getZ() > mapMaxZCoord)
			return null;

		//Check cardinal direction
		Province result = null;
		Province province;
		int[] x = new int[]{0,0,1,-1};
		int[] z = new int[]{-1,1,0,0};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
			if (province != null) {
				if(result == null) {
					result = province;
				} else if (province != result) {
					return null; //2 different adjacent provinces found. Return null, as this chunk will be a border.
				}
			}
		}

		if(result == null)
			return null; //No province found cardinally

		//Check non-cardinal
		x = new int[]{-1,1,1,-1};
		z = new int[]{-1,-1,1,1};
		for(int i = 0; i < 4; i++) {
			province = TownyProvincesDataHolder.getInstance().getProvinceAt(unclaimedCoord.getX() + x[i], unclaimedCoord.getZ() + z[i]);
			if (province != null && province != result) {
				return null; //2 different adjacent provinces found. Return null, as this chunk will be a border.
			}
		}

		return result;
	}
	
	public static Set<TPCoord> findAllAdjacentCoords(TPCoord targetCoord) {
		Set<TPCoord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			result.add(new TPFinalCoord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
		}
		return result;
	}

}

 