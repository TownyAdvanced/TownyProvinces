package io.github.townyadvanced.townyprovinces.integrations;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * PlaceholderAPI expansion exposing the TownyProvinces province at a player's
 * current location (#126).
 * <p>
 * Unlike Towny's own placeholders - which return the flat claim value from
 * config.yml - these report the real, region-set province cost (the same value
 * TownyProvinces charges and shows on the map).
 * <p>
 * Placeholders resolve from a per-player cache maintained by
 * {@link PlayerProvinceTracker} (so they are safe to query off the main thread)
 * and return an empty string when the player is not standing inside a province
 * (e.g. on a border, or outside the TownyProvinces world).
 *
 * <ul>
 *   <li>{@code %townyprovinces_is_province%} - true/false</li>
 *   <li>{@code %townyprovinces_province_id%} - internal province id</li>
 *   <li>{@code %townyprovinces_province_type%} - civilized / sea / wasteland</li>
 *   <li>{@code %townyprovinces_new_town_cost%} - province component of the new-town cost</li>
 *   <li>{@code %townyprovinces_total_new_town_cost%} - Towny base price + province component</li>
 *   <li>{@code %townyprovinces_upkeep_cost%} - province upkeep component</li>
 *   <li>{@code %townyprovinces_town%} - name of the town occupying the province, if any</li>
 * </ul>
 */
public class TownyProvincesPlaceholders extends PlaceholderExpansion {

	private final TownyProvinces plugin;

	public TownyProvincesPlaceholders(TownyProvinces plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull String getIdentifier() {
		return "townyprovinces";
	}

	@Override
	public @NotNull String getAuthor() {
		return "TownyAdvanced";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getPluginMeta().getVersion();
	}

	/**
	 * Keep the expansion registered across PlaceholderAPI reloads, since it is
	 * provided by this plugin rather than downloaded from the eCloud.
	 */
	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
		if (player == null || !TownyProvincesSettings.isTownyProvincesEnabled()) {
			return "";
		}
		//Resolved from a thread-safe cache rather than the player's live location:
		//PlaceholderAPI is frequently queried off the main thread, where Bukkit
		//entity/world access is unsafe (and illegal on Folia). See PlayerProvinceTracker.
		Province province = PlayerProvinceTracker.getProvince(player.getUniqueId());
		boolean inProvince = province != null;

		switch (params.toLowerCase(Locale.ROOT)) {
			case "is_province":
				return String.valueOf(inProvince);
			case "province_id":
				return inProvince ? province.getId() : "";
			case "province_type":
				return inProvince ? province.getType().name().toLowerCase(Locale.ROOT) : "";
			case "new_town_cost":
				return inProvince ? formatCost(getEffectiveNewTownCost(province)) : "";
			case "total_new_town_cost":
				//Mirror TownyListener: the province component is truncated to a whole
				//number first, then added to Towny's base new-town price.
				return inProvince ? formatCost(TownySettings.getNewTownPrice() + (int) getEffectiveNewTownCost(province)) : "";
			case "upkeep_cost":
				return inProvince ? formatCost(getEffectiveUpkeepCost(province)) : "";
			case "town":
				if (!inProvince) {
					return "";
				}
				Town town = province.getTownOrNull();
				return town != null ? town.getName() : "";
			default:
				//Unknown placeholder - let PlaceholderAPI show it unresolved
				return null;
		}
	}

	private double getEffectiveNewTownCost(Province province) {
		return TownyProvincesSettings.isBiomeCostAdjustmentsEnabled()
				? province.getBiomeAdjustedNewTownCost()
				: province.getNewTownCost();
	}

	private double getEffectiveUpkeepCost(Province province) {
		return TownyProvincesSettings.isBiomeCostAdjustmentsEnabled()
				? province.getBiomeAdjustedUpkeepTownCost()
				: province.getUpkeepTownCost();
	}

	/**
	 * Province costs are charged as whole numbers - TownyListener casts them to int
	 * (regionSettlementCost / regionUpkeepCost) - so the placeholders report them
	 * the same way.
	 */
	private String formatCost(double cost) {
		return String.valueOf((int) cost);
	}
}
