package io.github.townyadvanced.townyprovinces.integrations;

import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks, per online player, the TownyProvinces {@link Province} they are
 * currently standing in, so {@link TownyProvincesPlaceholders} can answer
 * without touching the Bukkit entity/world API.
 * <p>
 * PlaceholderAPI placeholders are commonly resolved off the main thread (TAB,
 * scoreboard and chat plugins do this by default). Calling
 * {@code player.getWorld()} / {@code player.getLocation()} from such a thread is
 * unsafe on Paper and is a hard error on Folia, where an entity may only be
 * accessed from its owning region thread. The province is therefore (re)computed
 * here - inside player event handlers, which always run on the correct thread for
 * that player on both Bukkit and Folia - and published into a thread-safe map
 * that the placeholder only reads.
 * <p>
 * Absence from the map means "not in a province" (on a border, or outside the
 * TownyProvinces world). {@link ConcurrentHashMap} cannot hold null values, so a
 * null province is stored as a removal rather than a mapping.
 */
public class PlayerProvinceTracker implements Listener {

	private static final ConcurrentHashMap<UUID, Province> PLAYER_PROVINCE = new ConcurrentHashMap<>();

	/**
	 * @return the province the given player currently stands in, or null if they
	 *         are in no province (border / non-TP world) or not yet tracked.
	 */
	@Nullable
	public static Province getProvince(UUID playerId) {
		return PLAYER_PROVINCE.get(playerId);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		refresh(event.getPlayer(), event.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getTo() != null) {
			refresh(event.getPlayer(), event.getTo());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null) {
			return;
		}
		//Province granularity is a Towny Coord (a chunk), so only recompute when the
		//player actually crosses a chunk boundary - PlayerMoveEvent fires on every
		//sub-block movement and look-around.
		if (from.getWorld() == to.getWorld()
				&& (from.getBlockX() >> 4) == (to.getBlockX() >> 4)
				&& (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) {
			return;
		}
		refresh(event.getPlayer(), to);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		PLAYER_PROVINCE.remove(event.getPlayer().getUniqueId());
	}

	/**
	 * Recompute and cache the player's province. Runs on the player's own thread
	 * (Bukkit main / Folia region), so location and world access here is safe.
	 */
	private void refresh(Player player, Location location) {
		UUID id = player.getUniqueId();
		World tpWorld = TownyProvincesSettings.getWorld();
		if (tpWorld == null || location.getWorld() == null || !location.getWorld().equals(tpWorld)) {
			PLAYER_PROVINCE.remove(id);
			return;
		}
		Coord coord = Coord.parseCoord(location);
		Province province = TownyProvincesDataHolder.getInstance().getProvinceAtCoord(coord.getX(), coord.getZ());
		if (province == null) {
			PLAYER_PROVINCE.remove(id);
		} else {
			PLAYER_PROVINCE.put(id, province);
		}
	}
}
