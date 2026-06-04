package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.scheduling.ScheduledTask;
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
	private static ScheduledTask landValidationScheduledTask = null;
	public static void startTask() {
		//Cancel any existing scheduled task first, so a double start can't orphan
		//a running async job whose handle we'd otherwise lose.
		if (landValidationScheduledTask != null) {
			landValidationScheduledTask.cancel();
		}
		landValidationTask = new LandvalidationTask();
		landValidationScheduledTask = TownyProvinces.getPlugin().getScheduler().runAsync(landValidationTask);
//		landValidationTask.runTaskAsynchronously(TownyProvinces.getPlugin());
		landValidationJobStatus = LandValidationJobStatus.STARTED;
		Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_started"));
	}
	
	public static void stopTask() {
		if(landValidationTask != null) {
			if (landValidationScheduledTask != null)
				landValidationScheduledTask.cancel();
			landValidationTask = null;
			landValidationScheduledTask = null;
			landValidationJobStatus = LandValidationJobStatus.STOPPED;
			Messaging.sendGlobalMessage(Translatable.of("msg_land_validation_job_stopped"));
		}
	}

	public static void pauseTask() {
		if(landValidationTask != null) {
			if (landValidationScheduledTask != null)
				landValidationScheduledTask.cancel();
			landValidationTask = null;
			landValidationScheduledTask = null;
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

 