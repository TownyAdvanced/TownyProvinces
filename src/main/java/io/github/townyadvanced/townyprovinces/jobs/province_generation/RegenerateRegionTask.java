package io.github.townyadvanced.townyprovinces.jobs.province_generation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.dynmap_display.DynmapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
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
	public final TPCoord searchCoord;
	
	public RegenerateRegionTask(String regionName) {
		this.regionName = regionName;
		this.searchCoord = new TPFreeCoord(0,0);
	}
	
	@Override
	public void run() {
		try {
			TownyProvinces.info("Regeneration Job Started");
			TownyProvinces.info("Regeneration Job: Acquiring dynmap display lock");
			synchronized (TownyProvinces.DYNMAP_DISPLAY_LOCK) {
				TownyProvinces.info("Regeneration Job: Dynmap display lock acquired");
				executeRegionRegenerationJob();
			}
		} finally {
			TownyProvinces.info("Regeneration Job: Dynmap display lock released");
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
			//Initialize the unclaimed coords map
			unclaimedCoordsMap = soonToBeUnclaimedCoords;
			//Paint all regions
			paintingSuccess = paintAllRegions();
		} else {
			//Initialize the unclaimed coords map
			unclaimedCoordsMap = TownyProvincesDataHolder.getInstance().getAllUnclaimedCoordsOnMap();
			//Paint one region
			paintingSuccess = paintOneRegion(regionName, true);
		}
		if(!paintingSuccess) {
			TownyProvinces.info("Problem Painting Regions");
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
	
	public boolean paintAllRegions() {
		//Paint all Regions
		boolean firstRegion = true;
		for (String regionName: TownyProvincesSettings.getOrderedRegionNames()) {
			if(firstRegion) {
				firstRegion = false;
				//Paint region
				if(!paintOneRegion(regionName, false)) {
					return false;
				}
			} else {
				//Paint region
				if(!paintOneRegion(regionName, true)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean paintOneRegion(String regionName, boolean deleteExistingProvincesInRegion) {
		PaintRegionAction regionPaintTask = new PaintRegionAction(regionName, unclaimedCoordsMap);
		return regionPaintTask.executeAction(deleteExistingProvincesInRegion);
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

 