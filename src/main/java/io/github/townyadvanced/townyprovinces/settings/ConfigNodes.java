package io.github.townyadvanced.townyprovinces.settings;

public enum 
ConfigNodes {
	
	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version.  Please do not edit."),
	LANGUAGE("language",
			"english.yml",
			"# The language file you wish to use"),
	ENABLED(
			"enabled",
				"true",
				"",
				"# If true, the TownyProvinces plugin is enabled."),
	WORLD_NAME(
			"world_name",
				"world",
				"",
				"# The name of the world where TownyProvinces appplies.",
				"# TIP: TP does not yet support multiple worlds"),

	PROVINCE_GENERATION(
		"province_generation",
		"",
		"",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |                Province Generation                   | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	TOP_LEFT_WORLD_CORNER_LOCATION(
		"province_generation.top_left_world_corner_location",
		"-2000,-2000",
		""),
	BOTTOM_RIGHT_WORLD_CORNER_LOCATION(
		"province_generation.bottom_right_world_corner_location",
		"2000,2000",
		""),
	AVERAGE_PROVINCE_SIZE_IN_SQUARE_METRES(
		"province_generation.average_province_size_in_square_metres",
		"250000",
		""),
	MIN_ALLOWED_DISTANCE_BETWEEN_PROVINCE_HOMEBLOCKS(
		"province_generation.min_allowed_distance_between_province_homeblocks",
		"200",
		""),

	MAX_ALLOWED_VARIANCE_BETWEEN_IDEAL_AND_ACTUAL_NUM_PROVINCES(
	"province_generation.max_allowed_distance_between_ideal_and_actual_num_provinces",
		"0.1",
		"");
	
	private final String Root;
	private final String Default;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}

}
