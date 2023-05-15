package io.github.townyadvanced.townyprovinces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.object.TranslationLoader;

public class TownyListener implements Listener {

	/* Handle reloading MetaData when Towny's DB is loaded. */
//	@EventHandler
	public void onTownyLoadDB(TownyLoadedDatabaseEvent event) {
	}

	/* Handle re-adding the lang string into Towny when Towny reloads the Translation Registry. */
	@EventHandler
	public void onTownyLoadLang(TranslationLoadEvent event) {
		Plugin plugin = TownyProvinces.getPlugin();
		if (!TownyProvinces.hasLocale())
			return;
		Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
		TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, TownyProvinces.class);
		loader.load();
		Map<String, Map<String, String>>  translations = loader.getTranslations();
		for (String language : translations.keySet())
			for (Map.Entry<String, String> map : translations.get(language).entrySet())
				event.addTranslation(language, map.getKey(), map.getValue());
	}
}
