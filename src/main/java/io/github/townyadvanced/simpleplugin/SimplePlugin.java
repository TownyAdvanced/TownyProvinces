package io.github.townyadvanced.simpleplugin;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.util.Version;

import io.github.townyadvanced.simpleplugin.settings.Settings;

public class SimplePlugin extends JavaPlugin {

	private static SimplePlugin plugin;
	private static Version requiredTownyVersion = Version.fromString("0.98.6.0");
	boolean hasConfig = false;
	boolean hasLocale = false;
	boolean hasListeners = true;

	@Override
	public void onEnable() {

		plugin = this;

		if (!townyVersionCheck()) {
			severe("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
			onDisable();
			return;
		} else {
			info("Towny version " + getTownyVersion() + " found.");
		}

		if ((hasConfig && !loadConfig())
		|| (hasLocale && !loadLocalization(false))) {
			onDisable();
			return;
		}

		if (hasListeners)
			registerListeners(Bukkit.getPluginManager());
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
			TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, SimplePlugin.class);
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

	private void registerListeners(PluginManager pm) {
		pm.registerEvents(new TownyListener(), this);
	}


	public String getVersion() {
		return this.getDescription().getVersion();
	}

	public static SimplePlugin getPlugin() {
		return plugin;
	}

	public static String getPrefix() {
		return "[" + plugin.getName() + "]";
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
	
	public static boolean hasLocale() {
		return plugin.hasLocale;
	}
}
