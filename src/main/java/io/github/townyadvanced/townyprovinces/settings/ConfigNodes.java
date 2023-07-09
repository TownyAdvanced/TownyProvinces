package io.github.townyadvanced.townyprovinces.settings;

public enum ConfigNodes {
	
	VERSION_HEADER("version", "", ""),
	VERSION(
		"version.version",
		"",
		"# This is the current version.  Please do not edit."),
	LANGUAGE(
		"language",
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
		"# TownyProvinces does not yet support multiple worlds"),
	PAUSE_MILLISECONDS_BETWEEN_BIOME_LOOKUPS(
		"pause_milliseconds_between_biome_lookups",
		"1000",
		"",
		"# Governs the pause between biome lookups.",
		"# A high value will make the landvalidation job run slow",
		"# A low value will make the landvalidation job take up lots of CPU"),
	MAX_NUM_TOWNBLOCKS_IN_EACH_FOREIGN_PROVINCE(
		"max_num_townblocks_in_each_foreign_province",
		"8",
		"",
		"# Determines how many townblocks a town can have in each foreign province.",
		"# These townblocks can only be unlocked by building an outpost in a sea or wasteland province."),
	BIOME_COST_ADJUSTMENTS(
		"biome_cost_adjustments",
		"",
		"",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |                 BIOME COST ADJUSTMENTS               | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	BIOME_COST_ADJUSTMENTS_ENABLED(
		"biome_cost_adjustments.enabled",
		"true",
		"",
		"# If this is true, then province costs are adjusted by the biomes contained in the province."),
	BIOME_COST_ADJUSTMENTS_WATER(
		"biome_cost_adjustments.water",
		"0.05",
		"",
		"# Assuming server doesn't allow modifying coastline, these chunks can only be settled by going underground."),
	BIOME_COST_ADJUSTMENTS_HOT_LAND(
		"biome_cost_adjustments.hot_land",
		"0.3",
		"",
		"# Desert. Hard to grow crops, can't find animals, and sand is easily griefable."),
	BIOME_COST_ADJUSTMENTS_COLD_LAND(
		"biome_cost_adjustments.cold_land",
		"0.1",
		"",
		"# Snow and ice. Very hard to live in."),
	PROVINCE_TYPES(
		"province_types",
		"",
		"",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |                   PROVINCE TYPES                   | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	CIVILIZED(
		"province_types.civilized",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                      CIVILIZED                       | #",
		"# +------------------------------------------------------+ #",
		""),
	CIVILIZED_BORDER_APPEARANCE(
		"province_types.civilized.border_appearance",
		"",
		""),
	CIVILIZED_BORDER_APPEARANCE_WEIGHT(
		"province_types.civilized.border_appearance.weight",
		"1",
		"",
		"# This value determines the weight of the border."),
	CIVILIZED_BORDER_APPEARANCE_OPACITY(
		"province_types.civilized.border_appearance.opacity",
		"1",
		"",
		"# This value determines the opacity of the border."),
	CIVILIZED_BORDER_APPEARANCE_COLOUR(
		"province_types.civilized.border_appearance.color",
		"0",
		"",
		"# This value, in hex format, determines the color of the border."),
	SEA(
		"province_types.sea",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                          SEA                         | #",
		"# +------------------------------------------------------+ #",
		""),
	SEA_OUTPOSTS_ALLOWED(
		"province_types.sea.foreign_outposts_allowed",
		"true",
		""),
	SEA_BORDER_APPEARANCE(
		"province_types.sea.border_appearance",
		"",
		""),
	SEA_BORDER_APPEARANCE_WEIGHT(
		"province_types.sea.border_appearance.weight",
		"1",
		"",
		"# This value determines the weight of the border."),
	SEA_BORDER_APPEARANCE_OPACITY(
		"province_types.sea.border_appearance.opacity",
		"0.1",
		"",
		"# This value determines the opacity of the border."),
	SEA_BORDER_APPEARANCE_COLOUR(
		"province_types.sea.border_appearance.color",
		"33FFFF",
		"",
		"# This value, in hex format, determines the color of the border."),
	WASTELAND(
		"province_types.wasteland",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                      WASTELAND                       | #",
		"# +------------------------------------------------------+ #",
		""),
	WASTELAND_OUTPOSTS_ALLOWED(
		"province_types.wasteland.foreign_outposts_allowed",
		"true",
		""),
	WASTELAND_BORDER_APPEARANCE(
		"province_types.wasteland.border_appearance",
		"",
		""),
	WASTELAND_BORDER_APPEARANCE_WEIGHT(
		"province_types.wasteland.border_appearance.weight",
		"1",
		"",
		"# This value determines the weight of the border."),
	WASTELAND_BORDER_APPEARANCE_OPACITY(
		"province_types.wasteland.border_appearance.opacity",
		"0.1",
		"",
		"# This value determines the opacity of the border."),
	WASTELAND_BORDER_APPEARANCE_COLOUR(
		"province_types.wasteland.border_appearance.color",
		"e60909",
		"",
		"# This value, in hex format, determines the color of the border."),
	TRAVEL(
		"travel",
		"",
		"",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |                         TRAVEL                       | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),	
	ROADS(
		"travel.roads",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                         ROADS                        | #",
		"# +------------------------------------------------------+ #",
		""),
	ROADS_ENABLED(
		"travel.roads.enabled",
		"true",
		"",
		"# If this value is true, then roads are enabled."),
	ROADS_MAX_FAST_TRAVEL_RANGE(
		"travel.roads.fast_travel_range",
		"5",
		"",
		"# This value determines the fast-travel range of ports, in number-of-homeblocks-traversed"),
	PORTS(
		"travel.ports",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                         PORTS                        | #",
		"# +------------------------------------------------------+ #",
		""),
	PORTS_ENABLED(
		"travel.ports.enabled",
		"true",
		"",
		"# If this value is true, then ports are enabled."),
	PORTS_PURCHASE_PRICE(
		"travel.ports.purchase_cost",
		"50",
		"",
		"# The value determines the purchase price of a port plot."),
	PORTS_UPKEEP_COST(
		"travel.ports.upkeep_cost",
		"5",
		"",
		"# The value determines the upkeep cost of a port plot.",
		"# This is on top of any normal plot upkeep cost."),
	PORTS_MAX_FAST_TRAVEL_RANGE(
		"travel.ports.fast_travel_range",
		"3000",
		"",
		"# This value determines the fast-travel range of ports"),
	JUMP_NODES(
		"travel.jump_nodes",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                     JUMP NODES                       | #",
		"# +------------------------------------------------------+ #",
		""),
	JUMP_NODES_ENABLED(
		"travel.jump_nodes.enabled",
		"true",
		"",
		"# If this value is true, then jump nodes are enabled."),
	JUMP_NODES_PURCHASE_PRICE(
		"travel.jump_nodes.purchase_cost",
		"200",
		"",
		"# The value determines the purchase price of a jump node."),
	JUMP_NODES_UPKEEP_COST(
		"travel.jump_nodes.upkeep_cost",
		"20",
		"",
		"# The value determines the upkeep cost of a jump node plot.",
		"# This is on top of any normal plot upkeep cost.");

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
