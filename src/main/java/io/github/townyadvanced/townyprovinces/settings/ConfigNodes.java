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
	PROVINCE_COST_LIMIT_PROPORTION(
		"province_cost_limit_proportion",
		"1.7",
		"",
		"# This value determines the cost limit of each province.",
		"# The value to both newTownCost and upkeepTownCost.",
		"# The value is a proportion of the 'average regional province price without outliers', if you know what I mean....",
		"# If you don't know what I mean, use caution when adjusting."),
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
		"0.9",
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
		"1",
		"",
		"# This value determines the opacity of the border."),
	WASTELAND_BORDER_APPEARANCE_COLOUR(
		"province_types.wasteland.border_appearance.color",
		"e60909",
		"",
		"# This value, in hex format, determines the color of the border."),
	MAP_INTEGRATION(
		"map_integration",
		"",
		"",
		"",
		"############################################################",
		"# +------------------------------------------------------+ #",
		"# |                MAP PLUGIN INTEGRATION                | #",
		"# +------------------------------------------------------+ #",
		"############################################################",
		""),
	MAP_REFRESH_PERIOD_MILLISECONDS(
		"map_integration.refresh_period_seconds",
		"30",
		"",
		"# The period between map refreshes.",
		"# A high value is softer on your CPU.",
		"# A low value means quicker map changes in response to things like province type or nation-color changes"),
	TOWN_COSTS_ICON(
		"map_integration.town_costs_icon",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                  TOWN COSTS ICON                     | #",
		"# +------------------------------------------------------+ #",
		""),
	MAP_TOWN_COSTS_ICON_URL(
		"map_integration.town_costs_icon.url",
		"https://cdn-icons-png.flaticon.com/512/9729/9729309.png", "",
		"# Icon for the town costs. This must be a valid image URL.",
		"# Default coin icon created by Md Tanvirul Haque - Flaticon",
		"# https://www.flaticon.com/free-icon/dollar_9729309"
		),
	MAP_TOWN_COSTS_ICON_HEIGHT(
		"map_integration.town_costs_icon.height",
		"35",
		"",
		"# Height in pixels for the town costs icon to be displayed as."),
	MAP_TOWN_COSTS_ICON_WIDTH(
		"map_integration.town_costs_icon.width",
		"35",
		"",
		"# Width in pixels for the town costs icon to be displayed as."),
	MAP_NATION_COLOURS(
		"map_integration.nation_colors",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                   NATION COLORS                      | #",
		"# +------------------------------------------------------+ #",
		""),
	MAP_NATION_COLOURS_ENABLED(
		"map_integration.province_nation_colors.enabled",
		"true",
		"",
		"# If this value is true, then a province containing a nation-town, gets filled with the map-color of that nation."),
	MAP_NATION_COLOURS_OPACITY(
		"map_integration.province_nation_colors.opacity",
		"0.2",
		"",
		"# This value determines the opacity of the province nation color."),
	DYNMAP(
		"map_integration.dynmap",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                        DYNMAP                        | #",
		"# +------------------------------------------------------+ #",
		""),
	DYNMAP_USE_TOWN_COSTS_ICON(
		"map_integration.dynmap.use_town_costs_icon",
		"false",
		"",
		"# Toggle whether Dynmap should use your specified town_costs_icon, or an inbuilt Dynmap icon."),
	PL3XMAP(
		"map_integration.pl3xmap",
		"",
		"",
		"",
		"# +------------------------------------------------------+ #",
		"# |                        PL3XMAP                       | #",
		"# +------------------------------------------------------+ #",
		""),
	PL3XMAP_PROVINCES_LAYER(
		"map_integration.pl3xmap.provinces_layer",
		"",
		""),
	PL3XMAP_PROVINCES_LAYER_PRIORITY(
		"map_integration.pl3xmap.provinces_layer.priority",
		"6",
		"",
		"# You do not need to change this unless other map addons conflict with the provinces layer."),
	PL3XMAP_PROVINCES_LAYER_ZINDEX(
		"map_integration.pl3xmap.provinces_layer.zindex",
		"250",
		"",
		"# You can decrease this value to blend the provinces layer with the map more."),
	PL3XMAP_PROVINCES_LAYER_TOGGLEABLE(
		"map_integration.pl3xmap.provinces_layer.toggleable",
		"true",
		"",
		"# Set to false to disallow users from toggling the provinces layer."),
	PL3XMAP_TOWN_COSTS_LAYER(
		"map_integration.pl3xmap.town_costs_layer",
		"",
		""),
	PL3XMAP_TOWN_COSTS_LAYER_PRIORITY(
		"map_integration.pl3xmap.town_costs_layer.priority",
		"6",
		"",
		"# You do not need to change this unless other map addons conflict with the town costs layer."),
	PL3XMAP_TOWN_COSTS_LAYER_ZINDEX(
		"map_integration.pl3xmap.town_costs_layer.zindex",
		"250",
		"",
		"# You can decrease this value to blend the town costs layer with the map more.");

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
