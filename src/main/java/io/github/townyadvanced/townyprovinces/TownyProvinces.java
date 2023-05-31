package io.github.townyadvanced.townyprovinces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.integrations.dynmap.DynmapIntegration;
import io.github.townyadvanced.townyprovinces.listeners.TownyListener;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.util.ProvinceGeneratorUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.util.Version;

import io.github.townyadvanced.townyprovinces.settings.Settings;

public class TownyProvinces extends JavaPlugin {
	private static TownyProvinces plugin;
	private static final Version requiredTownyVersion = Version.fromString("0.99.0.7");
	@Override
	public void onEnable() {
		plugin = this;

		//Setup Basics
		if(!checkTownyVersion()
				|| !loadConfig()
				|| !loadLocalization(false)
				|| !TownyProvincesSettings.isTownyProvincesEnabled() 
				|| !TownyProvincesDataHolder.initialize()
				|| !DataHandlerUtil.setupPluginSubFoldersIfRequired()
				|| !DataHandlerUtil.loadAllData()
				|| !registerListeners()) {
			onDisable();
			return;
		}
	
		//If the map is blank and there was no error, generate new provinces
		//This is useful as either a demo, or a way to regenerate all provinces
		if(TownyProvincesDataHolder.getInstance().getNumProvinces() == 0) {
			if(!ProvinceGeneratorUtil.generateProvinces()) {
				onDisable();
				return;
			}
		}
		
		DataHandlerUtil.saveAllData();
		
		//Load integrations 
		loadIntegrations();
	}

	private boolean loadIntegrations() {
		try {
			if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
				info("Found Dynmap plugin. Enabling Dynmap integration.");
				new DynmapIntegration();
				return true;
			} else {
				info("Did not find Dynmap plugin. Cannot enable Dynmap integration.");
				return false;
			}
		} catch (Exception e) {
			severe("Problem enabling Dynmap integration: " + e.getMessage());
			return false;
		}
	}
	
	private boolean checkTownyVersion() {
		if (!townyVersionCheck()) {
			severe("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
			return false;
		} else {
			info("Towny version " + getTownyVersion() + " found.");
			return true;
		}
	}
	
	private boolean loadConfig() {
		try {
			Settings.loadConfig();
		} catch (TownyInitException e) {
			e.printStackTrace();
			severe("Config.yml failed to load! Disabling!");
			return false;
		}
		info("Config.yml loaded successfully.");
		return true;
	}

	public static boolean loadLocalization(boolean reload) {
		try {
			Plugin plugin = getPlugin(); 
			Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
			TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, TownyProvinces.class);
			loader.load();
			TownyAPI.getInstance().addTranslations(plugin, loader.getTranslations());
		} catch (TownyInitException e) {
			e.printStackTrace();
			severe("Locale files failed to load! Disabling!");
			return false;
		}
		if (reload) {
			info(Translatable.of("msg_reloaded_lang").defaultLocale());
		}
		return true;
	}

	private boolean registerListeners() {
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new TownyListener(), this);
		return true;
	}
	public String getVersion() {
		return this.getDescription().getVersion();
	}

	public static TownyProvinces getPlugin() {
		return plugin;
	}

	/**
	 * Use this for debugging  TODO - Maybe remove, because for debugging we tend to use TownyProvinces.info() / severe()
	 * 
	 * @return plugin prefix
	 */
	public static String getPrefix() {
		return "&6[" + plugin.getName() + "]";
	}

	/**
	 * Use this in most cases, as it looks better
	 * @return plugin prefix
	 */
	public static String getTranslatedPrefix() {
		return Translatable.of("plugin_prefix").translate(Locale.ROOT);
	}
	
	private boolean townyVersionCheck() {
		return Version.fromString(getTownyVersion()).compareTo(requiredTownyVersion) >= 0;
	}

	private String getTownyVersion() {
		return Bukkit.getPluginManager().getPlugin("Towny").getDescription().getVersion();
	}

	public static void info(String message) {
		plugin.getLogger().info(message);
	}

	public static void severe(String message) {
		plugin.getLogger().severe(message);
	}
}
