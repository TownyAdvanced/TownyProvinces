package io.github.townyadvanced.townyprovinces.province_generation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.integrations.dynmap.DynmapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.scheduler.BukkitRunnable;

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
	private final String givenRegionName;  //Name given by job starter. Might be "All"
	private final int mapMinXCoord;
	private final int mapMaxXCoord;
	private final int mapMinZCoord;
	private final int mapMaxZCoord;
	public final TPCoord searchCoord;
	
	public RegenerateRegionTask(String givenRegionName) {
		this.givenRegionName = givenRegionName;
		//Create region definitions folder and sample files if needed
		if(!FileUtil.createRegionDefinitionsFolderAndSampleFiles()) {
			throw new RuntimeException("Problem creation region definitions folder and sample files");
		}
		//Reload region definitions
		if(!TownyProvincesSettings.loadRegionDefinitions()) {
			throw new RuntimeException("Problem reloading region definitions");
		}
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
		//Create data folder if needed
		if(!FileUtil.setupPluginDataFoldersIfRequired()) {
			throw new RuntimeException("Problem creating plugin data folders");
		}
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
		if(givenRegionName.equalsIgnoreCase("ALL")) {
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
			//Create and assign the map of unclaimed coords
			unclaimedCoordsMap = TownyProvincesDataHolder.getInstance().getAllUnclaimedCoordsInRegion(givenRegionName);
			//Paint one region
			PaintRegionAction regionPaintTask = new PaintRegionAction(givenRegionName, unclaimedCoordsMap);
			paintingSuccess = regionPaintTask.executeAction();
		}
		if(!paintingSuccess) {
			TownyProvinces.info("Problem Painting Regions");
			return;
		}
		//Allocate unclaimed chunks to provinces.
		//if(!assignUnclaimedCoordsToProvinces()) {
		//	TownyProvinces.info("Problem assigning unclaimed chunks to provinces");
		//	return;
		//}
		//Delete empty provinces
		if(!deleteEmptyProvinces()) {
			TownyProvinces.info("Problem deleting empty provinces");
			return;
		}
		//Save data and request full dynmap refresh
		DataHandlerUtil.saveAllData();
		DynmapDisplayTaskController.requestFullMapRefresh();
		//Messaging
		if(givenRegionName.equalsIgnoreCase("ALL")) {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_all_regions").translate(Locale.ROOT));
		} else {
			TownyProvinces.info(Translatable.of("msg_successfully_regenerated_one_regions", givenRegionName).translate(Locale.ROOT));
		}
		TownyProvinces.info("Region regeneration Job Complete"); //TODO - maybe global message?
	}
	
	public boolean paintAllRegions() {
		//Paint all Regions
		List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
		Collections.sort(regionNames); //Sort in alphabetical order
		for (String regionName: regionNames) {
			PaintRegionAction regionPaintTask = new PaintRegionAction(regionName, unclaimedCoordsMap);
			if(!regionPaintTask.executeAction()) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean deleteEmptyProvinces() {
		TownyProvinces.info("Now Deleting Empty Provinces.");
		for(Province province: new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet())) {
			if(province.getCoordsInProvince().size() == 0) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
			}
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

 