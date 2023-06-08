package io.github.townyadvanced.townyprovinces.settings;

import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

import java.nio.file.Path;

public class Settings {
	private static CommentedConfiguration config, newConfig;
	private static Path configPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve("config.yml");
	
	public static void loadConfig() {
		if (FileMgmt.checkOrCreateFile(configPath.toString())) {

			// read the config.yml into memory
			config = new CommentedConfiguration(configPath);
			if (!config.load())
				throw new TownyInitException("Failed to load config.yml.", TownyInitException.TownyError.MAIN_CONFIG);

			setDefaults(TownyProvinces.getPlugin().getVersion(), configPath);
			config.save();
		}
	}

	public static void addComment(String root, String... comments) {

		newConfig.addComment(root.toLowerCase(), comments);
	}

	private static void setNewProperty(String root, Object value) {

		if (value == null) {
			value = "";
		}
		newConfig.set(root.toLowerCase(), value.toString());
	}

	@SuppressWarnings("unused")
	private static void setProperty(String root, Object value) {

		config.set(root.toLowerCase(), value.toString());
	}
	
	/**
	 * Builds a new config reading old config data.
	 */
	private static void setDefaults(String version, Path configPath) {

		newConfig = new CommentedConfiguration(configPath);
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else
				setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
		}

		config = newConfig;
		newConfig = null;
	}
	
	public static String getString(String root, String def) {

		String data = config.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from config.yml");
			return "";
		}
		return data;
	}

	private static void sendError(String msg) {

		TownyProvinces.severe("Error could not read " + msg);
	}
	
	public static boolean getBoolean(ConfigNodes node) {

		return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	public static double getDouble(ConfigNodes node) {

		try {
			return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0.0;
		}
	}

	public static int getInt(ConfigNodes node) {

		try {
			return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0;
		}
	}

	public static String getString(ConfigNodes node) {

		return config.getString(node.getRoot().toLowerCase(), node.getDefault());
	}

}
