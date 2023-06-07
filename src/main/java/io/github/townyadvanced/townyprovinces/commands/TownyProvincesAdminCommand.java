package io.github.townyadvanced.townyprovinces.commands;

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
import io.github.townyadvanced.townyprovinces.util.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.util.ProvinceGeneratorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyProvincesAdminCommand implements TabExecutor {

	private static final List<String> adminTabCompletes = Arrays.asList("province","region", "seaprovincesjob");
	private static final List<String> adminTabCompletesProvince = Arrays.asList("sea","land");
	private static final List<String> adminTabCompletesRegion = Arrays.asList("regenerate");
	private static final List<String> adminTabCompletesSeaProvincesJob = Arrays.asList("start", "stop", "restart", "pause", "continue");

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
					List<String> regionOptions = new ArrayList<>();
					regionOptions.add("All");
					List<String> regionNames = new ArrayList<>(TownyProvincesSettings.getRegionDefinitions().keySet());
					Collections.sort(regionNames);
					regionOptions.addAll(regionNames);
					return NameUtil.filterByStart(regionOptions, args[2]);
				}
				break;
			case "seaprovincejob":
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
				case "seaprovincesjob":
					parseSeaProvincesJob(sender, StringMgmt.remFirstArg(args));
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
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpa", "province [sea|land] [<x>,<z>]", ""));
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpa", "region [regenerate] [<Region Name>]", ""));
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
		} else {
			showHelp(sender);
		}
	}

	private void parseSeaProvincesJob(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("start")) {
			
			
			parseProvinceSetToSeaCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("land")) {
			parseProvinceSetToLandCommand(sender,args);
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
		String caseCorrectRegionName = TownyProvincesSettings.getCaseCorrectRegionName(givenRegionName);
		if(givenRegionName.equalsIgnoreCase("all")) {
			if(!ProvinceGeneratorUtil.regenerateAllProvinces()) {
				Messaging.sendMsg(sender, Translatable.of("msg_problem_generating_all_provinces"));
				return;
			}
			DataHandlerUtil.saveAllData();
			TownyProvinces.getPlugin().getDynmapIntegration().requestMapClear();
		} else if(TownyProvincesSettings.getRegionDefinitions().containsKey(caseCorrectRegionName)) {
			if(!ProvinceGeneratorUtil.regenerateOneProvince(caseCorrectRegionName)) {
				Messaging.sendMsg(sender, Translatable.of("msg_problem_generating_one_province"));
				return;
			}
			DataHandlerUtil.saveAllData();
			TownyProvinces.getPlugin().getDynmapIntegration().requestMapClear();
		} else {
			Messaging.sendMsg(sender, Translatable.of("msg_err_unknown_region_name"));
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

