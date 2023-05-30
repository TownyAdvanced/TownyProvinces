package io.github.townyadvanced.townyprovinces.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PreNewTownEvent;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.event.EventHandler;
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
		event.setCancelMessage(TownyProvinces.getPrefix() + Translatable.of("msg_err_cannot_merge_towns").translate(Locale.ROOT));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onNewTownAttempt(PreNewTownEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		//No restriction if it is not in the TP-enabled world
		if (!event.getTownWorldCoord().getWorldName().equalsIgnoreCase(TownyProvincesSettings.getWorldName())) {
			return;
		}
		//Can't place new town outside of a province
		Coord coord = Coord.parseCoord(event.getTownLocation());
		ProvinceBlock provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
		if (provinceBlock == null) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getPrefix() + Translatable.of("msg_err_cannot_create_town_outside_province").translate(Locale.ROOT));
			return;
		}
		//Can't place new town on a province border
		if (provinceBlock.isProvinceBorder()) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getPrefix() + Translatable.of("msg_err_cannot_create_town_on_province_border").translate(Locale.ROOT));
			return;
		}
		//Can't place new town is province-at-location already has one
		if (doesProvinceContainTown(provinceBlock.getProvince())) {
			event.setCancelled(true);
			event.setCancelMessage(TownyProvinces.getPrefix() + Translatable.of("msg_err_cannot_create_town_in_full_province").translate(Locale.ROOT));
			return;
		}
	}

	private boolean doesProvinceContainTown(Province province) {
		String worldName = TownyProvincesSettings.getWorldName();
		WorldCoord worldCoord;
		for(ProvinceBlock provinceBlock: province.getProvinceBlocks()) {
			worldCoord = new WorldCoord(worldName, provinceBlock.getCoord());
			if(!TownyAPI.getInstance().isWilderness(worldCoord)) {
				return true;
			}
		}
		return false;
	}
}