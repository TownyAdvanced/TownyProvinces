package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
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
		/*
		 * If there are any requests pending, just start the job
		 * otherwise request all provinces
		 */
		if(!areAnyValidationsPending()) {
			setLandValidationRequestsForAllProvinces(true);
		}
		landValidationTask = new LandvalidationTask();
		landValidationTask.runTaskAsynchronously(TownyProvinces.getPlugin());
		landValidationJobStatus = LandValidationJobStatus.STARTED;
		Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_started"));
		return true;
	}
	
	public static void stopTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
			setLandValidationRequestsForAllProvinces(false);  //Clear all requests
			landValidationJobStatus = LandValidationJobStatus.STOPPED;
			Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_stopped"));
		}
	}

	public static void pauseTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
			landValidationJobStatus = LandValidationJobStatus.PAUSED;
			Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_paused"));
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

 