package io.github.townyadvanced.townyprovinces;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.util.Version;
import io.github.townyadvanced.townyprovinces.commands.TownyProvincesAdminCommand;
import io.github.townyadvanced.townyprovinces.data.DataHandlerUtil;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.jobs.dynmap_display.DynmapDisplayTaskController;
import io.github.townyadvanced.townyprovinces.listeners.TownyListener;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.settings.Settings;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class TownyProvinces extends JavaPlugin {
	/**
	 * Lock this if you want to change or display the map,
	 * to avoid concurrent modification problems
	 */
	public static final Object DYNMAP_DISPLAY_LOCK = new Object();
	private static TownyProvinces plugin;
	private static final Version requiredTownyVersion = Version.fromString("0.99.1.0");
	
	@Override
	public void onEnable() {
		plugin = this;

		//Load Mandatory stuff
		if(!checkTownyVersion()
				|| !loadConfig()
				|| !loadLocalization(false)
				|| !validateWorld()
				|| !TownyProvincesSettings.isTownyProvincesEnabled() 
				|| !TownyProvincesDataHolder.initialize()
				|| !FileUtil.setupPluginDataFoldersIfRequired()
				|| !FileUtil.createRegionDefinitionsFolderAndSampleFiles()
				|| !DataHandlerUtil.loadAllData()
				|| !registerListeners()
				|| !registerAdminCommands()) {
			severe("TownyProvinces Did Not Load Successfully.");
			onDisable();
			return;
		} 

		//Load optional stuff 
		loadIntegrations();

		info("TownyProvinces Loaded Successfully");
	}
	
	private boolean registerAdminCommands() {
		getCommand("townyprovincesadmin").setExecutor(new TownyProvincesAdminCommand());
		return true;
	}

	private boolean loadIntegrations() {
		try {
			if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
				info("Found Dynmap plugin. Enabling Dynmap integration.");
				DynmapDisplayTaskController.startTask();
				return true;
			} else {
				info("Did not find Dynmap plugin. Cannot enable Dynmap integration.");
				return false;
			}
		} catch (Exception e) {
			Messaging.sendErrorMsg(Bukkit.getConsoleSender(), "Problem enabling Dynmap integration: " + e.getMessage());
			e.printStackTrace();
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

	private boolean validateWorld() {
		info("Now validating world");
		World world = TownyProvincesSettings.getWorld();
		if(world != null) {
			info("World Validated");
			return true;
		} else {
			Messaging.sendErrorMsg(Bukkit.getConsoleSender(), Translatable.of("msg_err_unknown_world"));
			return false;
		}
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
	 * Use this in most cases, as it looks better
	 * @return plugin prefix
	 */
	public static String getTranslatedPrefix() {
		return Translatable.of("townyprovinces_plugin_prefix").translate(Locale.ROOT);
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
