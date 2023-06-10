package io.github.townyadvanced.townyprovinces.commands;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesPermissionNodes;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.land_validation_job.LandValidationJob;
import io.github.townyadvanced.townyprovinces.land_validation_job.LandValidationJobStatus;
import io.github.townyadvanced.townyprovinces.province_generation_job.RegionRegenerationJob;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyProvincesAdminCommand implements TabExecutor {

	private static final List<String> adminTabCompletes = Arrays.asList("province","region", "landvalidationjob");
	private static final List<String> adminTabCompletesProvince = Arrays.asList("sea","land");
	private static final List<String> adminTabCompletesRegion = Arrays.asList("regenerate", "newtowncost", "upkeeptowncost");
	private static final List<String> adminTabCompletesSeaProvincesJob = Arrays.asList("status", "start", "stop", "restart", "pause");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "province":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesProvince, args[1]);
				break;
			case "region":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesRegion, args[1]);
				if (args.length == 3) {
					TownyProvincesSettings.loadRegionDefinitions();
					List<String> regionOptions = new ArrayList<>();
					regionOptions.add("All");
					List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
					Collections.sort(regionNames);
					regionOptions.addAll(regionNames);
					return NameUtil.filterByStart(regionOptions, args[2]);
				}
				break;
			case "landvalidationjob":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesSeaProvincesJob, args[1]);
				break;
			default:
				if (args.length == 1)
					return NameUtil.filterByStart(adminTabCompletes, args[0]);
		}
		return Collections.emptyList();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		parseAdminCommand(sender, args);
		return true;
	}

	private void parseAdminCommand(CommandSender sender, String[] args) {
		/*
		 * Parse Command.
		 */
		if (args.length > 0) {
			if (sender instanceof Player && !sender.hasPermission(TownyProvincesPermissionNodes.TOWNYPROVINCES_ADMIN.getNode(args[0]))) {
				Messaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
				return;
			}
			switch (args[0]) {
				case "province":
					parseProvinceCommand(sender, StringMgmt.remFirstArg(args));
					break;
				case "region":
					parseRegionCommand(sender, StringMgmt.remFirstArg(args));
					break;
				case "landvalidationjob":
					parseLandValidationJobCommand(sender, StringMgmt.remFirstArg(args));
					break;

				/*
				 * Show help if no command found.
				 */
				default:
					showHelp(sender);
			}
		} else {
			if (sender instanceof Player && !sender.hasPermission(TownyProvincesPermissionNodes.TOWNYPROVINCES_ADMIN.getNode())) {
				Messaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
				return;
			}
			showHelp(sender);
		}
	}

	private void showHelp(CommandSender sender) {
		TownyMessaging.sendMessage(sender, ChatTools.formatTitle("/townyprovincesadmin"));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "province [sea|land] [<x>,<z>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [regenerate] [<Region Name>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [newtowncost] [<Region Name>] [amount]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "region [upkeeptowncost] [<Region Name>] [amount]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpra", "landvalidationjob [status|start|stop|restart|pause]", ""));
	}

	private void parseProvinceCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("sea")) {
			parseProvinceSetToSeaCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("land")) {
			parseProvinceSetToLandCommand(sender,args);
		} else {
			showHelp(sender);
		}
	}
	
	private void parseRegionCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("regenerate")) {
			parseRegionRegenerateCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("newtowncost")) {
			parseRegionSetNewTownCostCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("upkeeptowncost")) {
			parseRegionSetTownUpkeepCostCommand(sender, args);
		} else {
			showHelp(sender);
		}
	}

	private void parseLandValidationJobCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			showHelp(sender);
			return;
		}
		LandValidationJob landValidationJob = LandValidationJob.getLandValidationJob();
		if (args[0].equalsIgnoreCase("status")) {
			Translatable status = Translatable.of(landValidationJob.getLandValidationJobStatus().getLanguageKey());
			Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_status").append(status));
			
		} else if (args[0].equalsIgnoreCase("start")) {
			if (landValidationJob.getLandValidationJobStatus().equals(LandValidationJobStatus.STOPPED)
					|| landValidationJob.getLandValidationJobStatus().equals(LandValidationJobStatus.PAUSED)) {
				landValidationJob.setLandValidationJobStatus(LandValidationJobStatus.START_REQUESTED);
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_starting"));
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_stopped_or_paused"));
			}
		
		} else if (args[0].equalsIgnoreCase("stop")) {
			if (landValidationJob.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				landValidationJob.setLandValidationJobStatus(LandValidationJobStatus.STOP_REQUESTED);
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_stopping"));
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}
			
		} else if (args[0].equalsIgnoreCase("pause")) {
			if (landValidationJob.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				landValidationJob.setLandValidationJobStatus(LandValidationJobStatus.PAUSE_REQUESTED);
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_pausing"));
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}

		} else if (args[0].equalsIgnoreCase("restart")) {
			if (landValidationJob.getLandValidationJobStatus().equals(LandValidationJobStatus.STARTED)) {
				landValidationJob.setLandValidationJobStatus(LandValidationJobStatus.RESTART_REQUESTED);
				Messaging.sendMsg(sender, Translatable.of("msg_land_validation_job_restarting"));
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_command_not_possible_job_not_started"));
			}
		} else {
			showHelp(sender);
		}
	}
	
	private void parseProvinceSetToSeaCommand(CommandSender sender, String[] args) {
		try {	
			String[] locationAsArray = args[1].split(",");
			if(locationAsArray.length != 2) {
				Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
				showHelp(sender);
				return;
			}
			int x = Integer.parseInt(locationAsArray[0]);
			int y = Integer.parseInt(locationAsArray[1]);
			Coord coord = Coord.parseCoord(x,y);
			Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
			//Validate action
			if(province == null) {
				Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
				showHelp(sender);
				return;
			}
			if(province.isSea()) {
				Messaging.sendMsg(sender, Translatable.of("msg_province_already_sea"));
				return;
			}
			//Set province to be sea
			province.setSea(true);
			province.saveData();
			Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_set_to_sea"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}

	private void parseRegionRegenerateCommand(CommandSender sender, String[] args) {
		String givenRegionName = args[1];
		String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);
		if(givenRegionName.equalsIgnoreCase("all")) {
			RegionRegenerationJob.startJob(givenRegionName);
			//TODO SEND A MESSAGE SAYING JOB STARTED
		} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
			RegionRegenerationJob.startJob(caseCorrectRegionName);
			//TODO SEND A MESSAGE SAYING JOB STARTED
		} else {
			Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
		}
	}

	private void parseRegionSetNewTownCostCommand(CommandSender sender, String[] args) {
		try {
			String givenRegionName = args[1];
			int newTownCost = Integer.parseInt(args[2]);
			String formattedNewTownCost = TownyEconomyHandler.getFormattedBalance(newTownCost);
			String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);
			
			if(givenRegionName.equalsIgnoreCase("all")) {
				//Set cost for all provinces, regardless of region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					province.setNewTownCost(newTownCost);
					province.saveData();
				}
				TownyProvinces.getPlugin().getDynmapIntegration().requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_new_town_cost_set_for_all_regions", formattedNewTownCost));

			} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
				//Set cost for just one region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					if(TownyProvincesSettings.isProvinceInRegion(province, caseCorrectRegionName)) {
						province.setNewTownCost(newTownCost);
						province.saveData();
					}
				}
				TownyProvinces.getPlugin().getDynmapIntegration().requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_new_town_cost_set_for_one_region", caseCorrectRegionName, formattedNewTownCost));
				
			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
			}
		} catch (NumberFormatException nfe) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_value_must_be_and_integer"));
		}
	}
	
	private void parseRegionSetTownUpkeepCostCommand(CommandSender sender, String[] args) {
		try {
			String givenRegionName = args[1];
			int townCost = Integer.parseInt(args[2]);
			String formattedTownCost = TownyEconomyHandler.getFormattedBalance(townCost);
			String caseCorrectRegionName = TownyProvincesSettings.getCaseSensitiveRegionName(givenRegionName);

			if(givenRegionName.equalsIgnoreCase("all")) {
				//Set cost for all provinces, regardless of region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					province.setUpkeepTownCost(townCost);
					province.saveData();
				}
				TownyProvinces.getPlugin().getDynmapIntegration().requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_upkeep_town_cost_set_for_all_regions", formattedTownCost));

			} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
				//Set cost for just one region
				for (Province province : TownyProvincesDataHolder.getInstance().getProvincesSet()) {
					if(TownyProvincesSettings.isProvinceInRegion(province, caseCorrectRegionName)) {
						province.setUpkeepTownCost(townCost);
						province.saveData();
					}
				}
				TownyProvinces.getPlugin().getDynmapIntegration().requestHomeBlocksRefresh();
				Messaging.sendMsg(sender, Translatable.of("msg_upkeep_town_cost_set_for_one_region", caseCorrectRegionName, formattedTownCost));

			} else {
				Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
			}
		} catch (NumberFormatException nfe) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_value_must_be_and_integer"));
		}
	}
	
	private void parseProvinceSetToLandCommand(CommandSender sender, String[] args) {
		try {
			String[] locationAsArray = args[1].split(",");
			if(locationAsArray.length != 2) {
				Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
				showHelp(sender);
				return;
			}
			int x = Integer.parseInt(locationAsArray[0]);
			int y = Integer.parseInt(locationAsArray[1]);
			Coord coord = Coord.parseCoord(x,y);
			Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
			//Validate action
			if(province == null) {
				Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
				showHelp(sender);
				return;
			}
			if(!province.isSea()) {
				Messaging.sendMsg(sender, Translatable.of("msg_province_already_land"));
				return;
			}
			//Set province to be land
			province.setSea(false);
			province.saveData();
			Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_set_to_land"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}
	
}

