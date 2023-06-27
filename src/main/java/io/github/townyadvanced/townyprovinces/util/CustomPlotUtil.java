package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.command.TownyAdminCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlockData;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;

public class CustomPlotUtil {

	public static boolean registerCustomPlots() {
		if(TownyProvincesSettings.isRoadsEnabled()) {
			registerCustomPlot("Road", "R", 0);
		}
		if(TownyProvincesSettings.isPortsEnabled()) {
			registerCustomPlot("Port", "P", TownyProvincesSettings.getPortsPurchasePrice());
		}
		if(TownyProvincesSettings.isJumpNodesEnabled()) {
			registerCustomPlot("Jump-Node", "J", TownyProvincesSettings.getJumpNodesPurchasePrice());
		}

		//reload towny config to ensure the custom plot types are loaded correctly
		try {
			(new TownyAdminCommand(Towny.getPlugin())).parseTownyAdminCommand(Bukkit.getConsoleSender(), new String[]{"reload", "database"});
		} catch (TownyException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	public static void registerCustomPlot(String name, String mapCharacter, double cost) {
		if (TownBlockTypeHandler.exists(name)) {
			return;
		}
		TownBlockType customPlot = new TownBlockType(name, new TownBlockData() {
			@Override
			public String getMapKey() {
				return mapCharacter; // A single character to be shown on the /towny map and /towny map hud
			}
			@Override
			public double getCost() {
				return cost; //Cost of the plot
			}
		});
		try {
			TownBlockTypeHandler.registerType(customPlot);
		} catch (TownyException e) {
			TownyProvinces.severe(e.getMessage());
		}
	}
}
