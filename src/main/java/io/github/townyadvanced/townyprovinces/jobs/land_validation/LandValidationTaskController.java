package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;

public class LandValidationTaskController {
	private static LandValidationJobStatus landValidationJobStatus;

	static {
		if(areAnyValidationsPending()) {
			landValidationJobStatus = LandValidationJobStatus.PAUSED;
		} else {
			landValidationJobStatus = LandValidationJobStatus.STOPPED;
		}
	}

	private static LandvalidationTask landValidationTask = null;
	public static boolean startTask() {
		TownyProvinces.info("Land Validation Job Job Starting");
		landValidationTask = new LandvalidationTask();
		landValidationTask.runTask(TownyProvinces.getPlugin());
		landValidationJobStatus = LandValidationJobStatus.STARTED;
		TownyProvinces.info("Land Validation Job Job Started");
		return true;
	}
	
	public static void stopTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
			setLandValidationRequestsForAllProvinces(false);
			landValidationJobStatus = LandValidationJobStatus.STOPPED;
			TownyProvinces.info("Land Validation Job Stopped.");
		}
	}

	public static void pauseTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
			landValidationJobStatus = LandValidationJobStatus.PAUSED;
			TownyProvinces.info("Land Validation Job Paused.");
		}
	}

	public static void restartTask() {
		stopTask();
		startTask();
	}

	private static void setLandValidationRequestsForAllProvinces(boolean value) {
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.isLandValidationRequested() != value) {
				province.setLandValidationRequested(value);
				province.saveData();
			}
		}
	}

	public static LandValidationJobStatus getLandValidationJobStatus() {
		return landValidationJobStatus;
	}

	public static void setLandValidationJobStatus(LandValidationJobStatus status) {
		landValidationJobStatus = status;
	}
	
	private static boolean areAnyValidationsPending() {
		for(Province province: TownyProvincesDataHolder.getInstance().getProvincesSet()) {
			if(province.isLandValidationRequested()) {
				return true;
			}
		}
		return false;
	}
}

 