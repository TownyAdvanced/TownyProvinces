package io.github.townyadvanced.townyprovinces.jobs.land_validation;

import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.jobs.regenerate_region.RegenerateRegionTask;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import org.bukkit.command.CommandSender;

public class LandValidationTaskController {

	private static LandValidationTask landValidationTask = null;
	
	public static boolean startTask(CommandSender sender) {
		if (landValidationTask != null) {
			//TODO message job already started
			Messaging.sendMsg(sender, Translatable.of("msg_err_regeneration_job_already_started"));
			return false;
		} else {
			TownyProvinces.info("Land Validation Job Starting");
			landValidationTask = new LandValidationTask();
			landValidationTask.runTaskAsynchronously(TownyProvinces.getPlugin());
			TownyProvinces.info("Land Validation Job Started");
			//TODO send message land validation job started
			return true;
		}
	}

	public static void endTask() {
		if(landValidationTask != null) {
			landValidationTask.cancel();
			landValidationTask = null;
		}
	}

}

 