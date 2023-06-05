package io.github.townyadvanced.townyprovinces.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesPermissionNodes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyProvincesAdminCommand implements TabExecutor {

	private static final List<String> adminTabCompletes = Arrays.asList("province");
	private static final List<String> adminTabCompletesProvince = Arrays.asList("delete","restore");

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		switch (args[0].toLowerCase()) {
			case "province":
				if (args.length == 2)
					return NameUtil.filterByStart(adminTabCompletesProvince, args[1]);
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
		TownyMessaging.sendMessage(sender, ChatTools.formatCommand("Eg", "/tpa", "province [delete|restore] [x,z]", ""));
	}

	private void parseProvinceCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			showHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("delete")) {
			parseProvinceDeleteCommand(sender, args);
		} else if (args[0].equalsIgnoreCase("restore")) {
			parseProvinceRestoreCommand(sender,args);
		} else {
			showHelp(sender);
		}
	}
	
	private void parseProvinceDeleteCommand(CommandSender sender, String[] args) {
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
				Messaging.sendMsg(sender, Translatable.of("msg_province_already_deleted"));
				return;
			}
			//Delete province
			province.setSea(true);
			Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_deleted"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}

	private void parseProvinceRestoreCommand(CommandSender sender, String[] args) {
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
				Messaging.sendMsg(sender, Translatable.of("msg_province_already_restored"));
				return;
			}
			///Restore province
			province.setSea(false);
			Messaging.sendMsg(sender, Translatable.of("msg_province_successfully_restored"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			Messaging.sendMsg(sender, Translatable.of("msg_err_invalid_province_location"));
			showHelp(sender);
		}
	}
	
}

