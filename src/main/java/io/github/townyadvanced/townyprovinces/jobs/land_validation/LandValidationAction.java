package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

public class LandValidationAction {

	
	/**
	 * Go through each province,
	 * And decide if it is land or sea,
	 * then set the isSea boolean as appropriate
	 * <p>
	 * This method will not always work perfectly
	 * because it checks only a selection if the biomes.
	 * It does this because checking a biome is hard on the processor
	 * <p>
	 * Mistakes are expected,
	 * which is why server owners can run /tp province sea x,y
	 */
	private void executeLandValidation() {
		TownyProvinces.info("Now Running land validation job.");
		int numProvincesNotRequired = 0;
		List<Province> provinces = TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList();
		for(Province province : provinces) {
			if(!province.isLandValidationRequested())
				numProvincesNotRequired++;
		}
		if(numProvincesNotRequired == provinces.size()) {
			//Nothing is scheduled. This must be a new (not unpaused) run
			setLandValidationRequestsForAllProvinces(true);
			numProvincesNotRequired = 0;
		}
		double numProvincesProcessed = numProvincesNotRequired;
		for(Province province: provinces) {
			if (province.isLandValidationRequested()) {
				boolean isSea = isProvinceMainlyOcean(province);
				if(isSea != province.isSea()) {
					province.setSea(isSea);
				}
				province.setLandValidationRequested(false);
				province.saveData();
				numProvincesProcessed++;
			}
			int percentCompletion = (int) ((numProvincesProcessed / provinces.size()) * 100);
			TownyProvinces.info("Land Validation Job Progress: " + percentCompletion + "%");

			//Handle any stop requests
			switch (LandValidationTaskController.getJobStatus()) {
				case LandValidationJobStatus.STOP_REQUESTED:
					setLandValidationRequestsForAllProvinces(false);
					LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.STOPPED);
					TownyProvinces.info("Land Validation Job Stopped.");
					return;
				case LandValidationJobStatus.PAUSE_REQUESTED:
					LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.PAUSED);
					TownyProvinces.info("Land Validation Job Paused.");
					return;
				case LandValidationJobStatus.RESTART_REQUESTED:
					setLandValidationRequestsForAllProvinces(false);
					LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.START_REQUESTED);
					TownyProvinces.info("Land Validation Job Stopped.");
					return;
			}
		}
		LandValidationTaskController.setLandValidationJobStatus(LandValidationJobStatus.STOPPED);
		TownyProvinces.info("Land Validation Job Complete.");
	}

	private void setLandValidationRequestsForAllProvinces(boolean value) {
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.isLandValidationRequested() != value) {
				province.setLandValidationRequested(value);
				province.saveData();
			}
		}
	}
	
	private static boolean isProvinceMainlyOcean(Province province) {
		List<TPCoord> coordsInProvince = province.getCoordsInProvince();
		String worldName = TownyProvincesSettings.getWorldName();
		World world = Bukkit.getWorld(worldName);
		Biome biome;
		TPCoord coordToTest;
		for(int i = 0; i < 10; i++) {
			coordToTest = coordsInProvince.get((int)(Math.random() * coordsInProvince.size()));
			int x = (coordToTest.getX() * TownyProvincesSettings.getChunkSideLength()) + 8;
			int z = (coordToTest.getZ() * TownyProvincesSettings.getChunkSideLength()) + 8;
			biome = world.getHighestBlockAt(x,z).getBiome();
			System.gc();
			try {
				//Sleep as the above check is hard on processor
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if(!biome.name().toLowerCase().contains("ocean") && !biome.name().toLowerCase().contains("beach")) {
				return false;
			}
		}
		return true;
	}
}
