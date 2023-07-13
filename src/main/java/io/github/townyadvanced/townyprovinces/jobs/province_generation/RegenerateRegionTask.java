package io.github.townyadvanced.townyprovinces.jobs.province_generation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.map_display.MapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.Region;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFinalCoord;
import io.github.townyadvanced.townyprovinces.objects.TPFreeCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.MoneyUtil;
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
	private final String givenRegionName;  //This will either be the case correct name of a real region, or "All"
	public final TPCoord searchCoord;
	
	public RegenerateRegionTask(String givenRegionName) {
		this.givenRegionName = givenRegionName;
		this.searchCoord = new TPFreeCoord(0,0);
	}
	
	@Override
	public void run() {
		try {
			TownyProvinces.info("Regeneration Job Started");
			TownyProvinces.info("Regeneration Job: Getting synch locks");
			synchronized (TownyProvinces.LAND_VALIDATION_JOB_LOCK) {
				synchronized (TownyProvinces.MAP_DISPLAY_JOB_LOCK) {
					synchronized (TownyProvinces.REGION_REGENERATION_JOB_LOCK) {
						synchronized (TownyProvinces.PRICE_RECALCULATION_JOB_LOCK) {
							TownyProvinces.info("Regeneration Job: Synch locks acquired");
							executeRegionRegenerationJob();
						}
					}
				}
			}
		} finally {
			RegenerateRegionTaskController.endTask();
			TownyProvinces.info("Regeneration Job Completed");
		}
	}
	
	public void executeRegionRegenerationJob() {
		//Paint region(s)
		boolean paintingSuccess;
		if(givenRegionName.equalsIgnoreCase("ALL")) {
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
			Region region = TownyProvincesSettings.getRegion(givenRegionName);
			paintingSuccess = paintOneRegion(region, true);
		}
		if(!paintingSuccess) {
			TownyProvinces.info("Problem Painting Regions");
			return;
		}
		//Recalculated all prices
		MoneyUtil.recalculateProvincePrices();
		//Save data and request full map refresh
		DataHandlerUtil.saveAllData();
		MapDisplayTaskController.requestFullMapRefresh();
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
		boolean firstRegionProcessed = false;
		for (Region region: TownyProvincesSettings.getOrderedRegionsList()) {
			if(!firstRegionProcessed) {
				firstRegionProcessed = true;
				//Paint region
				if(!paintOneRegion(region, false)) {
					return false;
				}
			} else {
				//Paint region
				if(!paintOneRegion(region, true)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean paintOneRegion(Region region, boolean deleteExistingProvincesInRegion) {
		PaintRegionAction regionPaintTask = new PaintRegionAction(region, unclaimedCoordsMap);
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

 