package io.github.townyadvanced.townyprovinces.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.metadata.TownMetaDataController;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * 
 * @author LlmDl
 *
 */
public class BukkitListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onSignChangeEvent(SignChangeEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled())
			return;
		String line1 = event.getLine(0);
		if (line1 == null || !line1.trim().equals(">>>"))
			return;
		if (TownyAPI.getInstance().isWilderness(event.getBlock())) {
			//Can't create sign in the wilderness
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_signs_except_in_travel_hubs"));
			event.setCancelled(true);
			return;
		}
		TownBlock townBlock = TownyAPI.getInstance().getTownBlock(event.getBlock().getLocation());
		if (townBlock == null
			|| townBlock.getType() == null
			|| townBlock.getTypeName() == null
			|| townBlock.getTownOrNull() == null
			|| townBlock.getTownOrNull().getHomeBlockOrNull() == null) {
			//Can't create the sign if the townblock or town data is dodgy
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_bad_town_data"));
			event.setCancelled(true);
			return;
		}
		Town town = townBlock.getTownOrNull();
		WorldCoord worldCoordOfSign = WorldCoord.parseWorldCoord(event.getBlock());
		String townBlockTypeNameLowercase = townBlock.getTypeName().toLowerCase();
		if (!worldCoordOfSign.equals(town.getHomeBlockOrNull().getWorldCoord())
			&& !townBlockTypeNameLowercase.equals("outpost")
			&& !townBlockTypeNameLowercase.equals("port")
			&& !townBlockTypeNameLowercase.equals("jump-node")) {
			//Can only create travel node in certain plot types
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_signs_except_in_travel_hubs"));
			event.setCancelled(true);
			return;
		}
		String line2 = event.getLine(1);
		if (line2 == null || !line2.trim().equalsIgnoreCase("fast travel to")) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_incorrect_second_line"));
			return;
		}
		String line3 = event.getLine(2);
		if (line3 == null || line3.length() == 0) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_unknown_destination_town", ""));
			return;
		}
		Town destinationTown = TownyAPI.getInstance().getTown(line3.trim());
		if (destinationTown == null) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_unknown_destination_town", line3.trim()));
			return;
		}
		if (town.equals(destinationTown)) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_source_town_and_destination_town_are_the_same", line3.trim()));
			return;
		}
		boolean destinationHasJumpHub = TownMetaDataController.hasJumpHub(destinationTown);
		if (!destinationHasJumpHub) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_no_jump_node_at_destination_town", line3.trim()));
			return;
		}
		//Sign creation successful
		Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_success_fast_travel_sign_created", line3.trim()));
	}
}
	

