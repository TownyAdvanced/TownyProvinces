package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;

public class LandValidationTaskController {
	private static LandValidationJobStatus landValidationJobStatus;

	static {
		//Could actually be paused, so this could be misleading
		//Todo - Maybe improve in future. You would need to do a read in a separate thread to determine if paused
		landValidationJobStatus = LandValidationJobStatus.STOPPED;
	}

	private static LandvalidationTask landValidationTask = null;
	public static void startTask() {
		landValidationTask = new LandvalidationTask();
		landValidationTask.runTaskAsynchronously(TownyProvinces.getPlugin());
		landValidationJobStatus = LandValidationJobStatus.STARTED;
		Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_started"));
	}
	
	public static void stopTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
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

	public static LandValidationJobStatus getLandValidationJobStatus() {
		return landValidationJobStatus;
	}

	public static void setLandValidationJobStatus(LandValidationJobStatus status) {
		landValidationJobStatus = status;
	}
	
}

 