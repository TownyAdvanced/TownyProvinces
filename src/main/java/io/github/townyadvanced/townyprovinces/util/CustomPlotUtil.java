package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlockData;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;

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
