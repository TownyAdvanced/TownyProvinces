package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.map_display.MapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceType;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.BiomeUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LandvalidationTask extends BukkitRunnable {
	
	@Override
	public void run() {
		TownyProvinces.info("Acquiring synch locks.");
		synchronized (TownyProvinces.LAND_VALIDATION_JOB_LOCK) {
			synchronized (TownyProvinces.REGION_REGENERATION_JOB_LOCK) {
				synchronized (TownyProvinces.PRICE_RECALCULATION_JOB_LOCK) {
					TownyProvinces.info("Synch locks acquired.");
					/*
					 * If there are no requests pending,
					 * this is a fresh start, so request all provinces
					 */
					if (!areAnyValidationsPending()) {
						setLandValidationRequestsForAllProvinces(true);
					}
					executeLandValidation();
				}
			}
		}
	}

	private boolean areAnyValidationsPending() {
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.isLandValidationRequested()) {
				return true;
			}
		}
		return false;
	}
	
	private void setLandValidationRequestsForAllProvinces(boolean value) {
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.isLandValidationRequested() != value) {
				province.setLandValidationRequested(value);
				province.saveData();
			}
		}
	}
	
	/**
	 * Go through each province,
	 * And decide if it is land or sea,
	 * then set the isSea boolean as appropriate
	 * <p>
	 * This method will not always work perfectly
	 * because it checks only a selection if the chunks in the province.
	 * It does this because checking a biome is hard on the processor
	 * <p>
	 * Mistakes are expected,
	 * which is why server owners can run /tp province sea [x,y] ([x2,y2])
	 */
	private void executeLandValidation() {
		TownyProvinces.info("Now Running land validation job.");
		double numProvincesProcessed = 0;
		Set<Province> copyOfProvincesSet = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvincesSet());
		for(Province province : copyOfProvincesSet) {
			if(!province.isLandValidationRequested())
				numProvincesProcessed++;  //Already processed
		}
		for(Province province: copyOfProvincesSet) {
			if (province.isLandValidationRequested()) {
				doLandValidation(province);
				numProvincesProcessed++;
			}
			int percentCompletion = (int)((numProvincesProcessed / copyOfProvincesSet.size()) * 100);
			TownyProvinces.info("Land Validation Job Progress: " + percentCompletion + "%");

			//Handle any stop requests
			LandValidationJobStatus landValidationJobStatus = LandValidationTaskController.getLandValidationJobStatus();
			switch (landValidationJobStatus) {
				case STOP_REQUESTED:
					TownyProvinces.info("Land Validation Task: Clearing all validation requests");
					setLandValidationRequestsForAllProvinces(false);  //Clear all requests
					TownyProvinces.info("Land Validation Task: Stopping");
					LandValidationTaskController.stopTask();
					return;
				case PAUSE_REQUESTED:
					TownyProvinces.info("Land Validation Task: Pausing");
					LandValidationTaskController.pauseTask();
					return;
				case RESTART_REQUESTED:
					TownyProvinces.info("Land Validation Task: Clearing all validation requests");
					setLandValidationRequestsForAllProvinces(false);  //Clear all requests
					TownyProvinces.info("Land Validation Task: Saving data");
					LandValidationTaskController.restartTask();
					return;
			}
		}
		LandValidationTaskController.stopTask();
		TownyProvinces.info("Land Validation Job Complete.");
	}

	/**
	 * 1. Record the proportions of different lands
	 * 2. Set the province to land or sea, depending on the result
	 * 3. Save
	 * @param province
	 */
	private void doLandValidation(Province province) {
		List<TPCoord> coordsInProvince = province.getListOfCoordsInProvince();
		World world = Bukkit.getWorld(TownyProvincesSettings.getWorldName());
		TPCoord coordToTest;
		double goodLand = 0;
		double water = 0;
		double hotLand = 0;
		double coldLand = 0;
		double totalChunksToScan = 20;

		for(int i = 0; i < totalChunksToScan; i++) {
			coordToTest = coordsInProvince.get((int) (Math.random() * coordsInProvince.size()));
			BiomeType biomeType = BiomeUtil.getBiomeType(world, coordToTest);
			switch (Objects.requireNonNull(biomeType)) {
				case GOOD_LAND:
					goodLand++;
					break;
				case WATER:
					water++;
					break;
				case HOT_LAND:
					hotLand++;
					break;
				case COLD_LAND:
					coldLand++;
					break;
			}
		}
		
		//Set type
		if(water == totalChunksToScan) {
			province.setType(ProvinceType.SEA);
		} else if (goodLand == 0) {
			province.setType(ProvinceType.WASTELAND);
		} else {
			province.setType(ProvinceType.CIVILIZED);
		}
			
		//Set proportions
		province.setEstimatedProportionOfGoodLand(goodLand / totalChunksToScan);
		province.setEstimatedProportionOfWater(water / totalChunksToScan);
		province.setEstimatedProportionOfHotLand(hotLand / totalChunksToScan);
		province.setEstimatedProportionOfColdLand(coldLand / totalChunksToScan);

		//Mark as validated
		province.setLandValidationRequested(false);
		
		//Save data
		province.saveData();
		
		//Request dynmap refresh of homeblocks 
		// (no need to refresh border colour changes, the dynmap task will do that where needed)
		MapDisplayTaskController.requestHomeBlocksRefresh();
	}
}
