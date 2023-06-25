package io.github.townyadvanced.townyprovinces.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.event.PreNewTownEvent;
import com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.metadata.TownMetaDataController;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.TPCoord;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.CustomPlotUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

public class TownyListener implements Listener {

	/* Handle reloading MetaData when Towny's DB is loaded. */
//	@EventHandler
	public void onTownyLoadDB(TownyLoadedDatabaseEvent event) {
	}
	
	/* Handle re-adding the lang string into Towny when Towny reloads the Translation Registry. */
	@EventHandler
	public void onTownyLoadLang(TranslationLoadEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		Plugin plugin = TownyProvinces.getPlugin();
		Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
		TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, TownyProvinces.class);
		loader.load();
		Map<String, Map<String, String>> translations = loader.getTranslations();
		for (String language : translations.keySet())
			for (Map.Entry<String, String> map : translations.get(language).entrySet())
				event.addTranslation(language, map.getKey(), map.getValue());
	}

	@EventHandler(ignoreCancelled = true)
	public void onTownMergeAttempt(TownPreMergeEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		event.setCancelled(true);
		event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + Translatable.of("msg_err_cannot_merge_towns").translate(Locale.ROOT));
	}

	/**
	 * Highest priority as we will assume success and take money here
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onNewTownAttempt(PreNewTownEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		//No restriction if it is not in the TP-enabled world
		if (!event.getTownWorldCoord().getWorldName().equalsIgnoreCase(TownyProvincesSettings.getWorldName())) {
			return;
		}
		/*
		 * Can't place new town outside a province
		 * Note: unless some hacking has occurred,
		 * the only possible place, this can occur,
		 * is on a province border.
		 */
		Coord coord = Coord.parseCoord(event.getTownLocation());
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord.getX(), coord.getZ());
		if (province == null) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + Translatable.of("msg_err_cannot_create_town_on_province_border").translate(Locale.ROOT));
			return;
		}
		//Can't place town in Sea province
		if (province.isSea()) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + Translatable.of("msg_err_cannot_create_town_in_sea_provinces").translate(Locale.ROOT));
			return;
		}
		//Can't place new town is province-at-location already has one
		if (doesProvinceContainTown(province)) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + Translatable.of("msg_err_cannot_create_town_in_full_province").translate(Locale.ROOT));
			return;
		}
		/*
		 * Check if the player has enough money
		 * If the player does, pay the region settlement cost
		 */
		if (TownySettings.isUsingEconomy() && province.getNewTownCost() > 0) {
			double regionSettlementCost = province.getNewTownCost();
			double newTownCost = TownySettings.getNewTownPrice() + regionSettlementCost;
			Resident resident = TownyAPI.getInstance().getResident(event.getPlayer());
			if (resident != null && resident.getAccountOrNull() != null) {
				if (resident.getAccountOrNull().canPayFromHoldings(newTownCost)) {
					//Pay the region settlement cost
					resident.getAccountOrNull().withdraw(regionSettlementCost, "Region Settlement Cost");
					Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_you_paid_region_settlement_cost", TownyEconomyHandler.getFormattedBalance(regionSettlementCost)));
				} else {
					//Cancel the event
					event.setCancelled(true);
					event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + Translatable.of("msg_err_cannot_afford_new_town", TownyEconomyHandler.getFormattedBalance(newTownCost)).translate(Locale.ROOT));
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void on(TownUpkeepCalculationEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled() || !TownySettings.isUsingEconomy()) {
			return;
		}
		//Can't work it if the town has no homeblock
		if (!event.getTown().hasHomeBlock()) {
			return;
		}
		//No extra upkeep if it is not in the TP-enabled world
		if (!event.getTown().hasHomeBlock() && !event.getTown().getWorld().getName().equalsIgnoreCase(TownyProvincesSettings.getWorldName())) {
			return;
		}
		//Add extra upkeep
		Coord coord = event.getTown().getHomeBlockOrNull().getCoord();
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord.getX(),coord.getZ());
		if(province != null) {
			double regionUpkeepCost = province.getUpkeepTownCost();
			if (regionUpkeepCost > 0) {
				double updatedUpkeepCost = event.getUpkeep() + regionUpkeepCost;
				event.setUpkeep(updatedUpkeepCost);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTownClaimAttempt(TownPreClaimEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		//No restriction if it is not in the TP-enabled world
		if (!event.getTownBlock().getWorld().getName().equalsIgnoreCase(TownyProvincesSettings.getWorldName())) {
			return;
		}
		/*
		 * Can't clam outside a province
		 * Note: unless some hacking has occurred,
		 * the only possible place, this can occur,
		 * is on a province border.
		 */
		Province provinceAtClaimLocation = TownyProvincesDataHolder.getInstance().getProvinceAt(event.getTownBlock().getX(), event.getTownBlock().getZ());
		if (provinceAtClaimLocation == null) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + " " + Translatable.of("msg_err_cannot_claim_land_on_province_border").translate(Locale.ROOT));
			return;
		}
		//Can't claim without homeblock
		if(!event.getTown().hasHomeBlock()) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + " " + Translatable.of("msg_err_cannot_claim_land_without_homeblock").translate(Locale.ROOT));
			return;
		}
		//Can't claim outside town's province
		Province provinceOfClaimingTown = TownyProvincesDataHolder.getInstance().getProvinceAt(event.getTown().getHomeBlockOrNull().getX(), event.getTown().getHomeBlockOrNull().getZ());
		if(!provinceOfClaimingTown.equals(provinceAtClaimLocation)) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getTranslatedPrefix() + " " + Translatable.of("msg_err_cannot_claim_land_outside_own_province").translate(Locale.ROOT));
			return;
		}
	}

	/**
	 * - When a plot is about to be converted to a jump hub
	 *   first check the town metadata
	 *    - If the jump hub metadata exists, forbid a second one
	 *    - If no jump hub metadata exists, add the jump hub metadata
	 *    
	 * - When a plot is about to be converted FROM a jump hub, (PlotChangeTypeEvent)
	 *   Remove the jump hub metadata if it exists	 
	 *    
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true)
	private void onPlotConversion(PlotPreChangeTypeEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		if (TownyProvincesSettings.isJumpNodesEnabled()) {
			evaluatePlotConversionForJumpNodes(event);
		}
	}
	
	private void evaluatePlotConversionForJumpNodes(PlotPreChangeTypeEvent event) {
		Town town = event.getTownBlock().getTownOrNull();
		if(town == null) {
			return;
		}
		boolean oldTypeIsJumpHub = event.getOldType().getName().equalsIgnoreCase("jump-hub");
		boolean newTypeIsJumpHub = event.getNewType().getName().equalsIgnoreCase("jump-hub");
		boolean jumpHubMetadataExists = TownMetaDataController.hasJumpHub(town);
		
		if(oldTypeIsJumpHub) {
			if(!newTypeIsJumpHub) {
				//Jump hub deleted
				TownMetaDataController.removeJumpHubCoord(town);
				town.save();
				//TODO - Should we delete/break the signs at the plot?
			}
		} else {
			if(newTypeIsJumpHub) {
				if(jumpHubMetadataExists) {
					//Can't add a second jump hub
					event.setCancelled(true);
					event.setCancelMessage(Translatable.of("msg_err_cannot_add_a_second_jump_hub").translate(Locale.ROOT));
				} else {
					//Jump hub added to town
					TownMetaDataController.addJumpHubCoord(town, event.getTownBlock().getWorldCoord());
					town.save();
				}
			}
		}

		
		
	} 
	
	
	private boolean doesProvinceContainTown(Province province) {
		String worldName = TownyProvincesSettings.getWorldName();
		WorldCoord worldCoord;
		for(TPCoord coord: province.getListOfCoordsInProvince()) {
			worldCoord = new WorldCoord(worldName, coord.getX(), coord.getZ());
			if(!TownyAPI.getInstance().isWilderness(worldCoord)) {
				return true;
			}
		}
		return false;
	}

	// Re-register the TownBlockType when/if Towny reloads itself.
	@EventHandler
	public void onTownyLoadTownBlockTypes(TownBlockTypeRegisterEvent event) {
		CustomPlotUtil.registerCustomPlots();
	}

}