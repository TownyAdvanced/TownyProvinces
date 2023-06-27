package io.github.townyadvanced.townyprovinces.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.metadata.TownMetaDataController;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import io.github.townyadvanced.townyprovinces.util.FastTravelUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BukkitListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onSignChangeEvent(SignChangeEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		String line1 = event.getLine(0);
		if (line1 == null || !line1.trim().equals(">>>")) {
			return;
		}
		if (TownyAPI.getInstance().isWilderness(event.getBlock())) {
			//Can't create sign in the wilderness
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_signs_except_in_travel_plots"));
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
		String townBlockTypeNameLowercase = townBlock.getTypeName().toLowerCase();
		if (
			//!townBlock.isHomeBlock()
			//&& !townBlockTypeNameLowercase.equals("outpost")
			!townBlockTypeNameLowercase.equals("port")
			&& !townBlockTypeNameLowercase.equals("jump-node")) {
			//Can only create travel node in certain plot types
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_signs_except_in_travel_plots"));
			event.setCancelled(true);
			return;
		}
		String line2 = event.getLine(1);
		if (line2 == null || !line2.trim().equalsIgnoreCase("fast travel to")) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_incorrect_second_line"));
			return;
		}
		String destinationTownName = event.getLine(2);
		if (destinationTownName == null || destinationTownName.length() == 0) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_unknown_destination_town", destinationTownName));
			return;
		}
		Town destinationTown = TownyAPI.getInstance().getTown(destinationTownName.trim());
		if (destinationTown == null) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_unknown_destination_town", destinationTownName));
			return;
		}
		Town sourceTown = townBlock.getTownOrNull();
		if (sourceTown.equals(destinationTown)) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_source_town_and_destination_town_are_the_same", destinationTownName));
			return;
		}
		
		if(townBlockTypeNameLowercase.equals("jump-node")) {
			if(TownMetaDataController.getJumpNodeSigns(sourceTown).get(destinationTownName) != null) {
				event.setCancelled(true);
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_sign_already_exists_at_jump_node", destinationTownName));
				return;
			}
			//Sign creation successful
			TownMetaDataController.addJumpNodeSign(sourceTown, event.getBlock(), destinationTown.getName());
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_success_fast_travel_sign_created", destinationTownName));
		
		} else if (townBlockTypeNameLowercase.equals("port")) {
			if(TownMetaDataController.getPortSigns(sourceTown).get(destinationTownName) != null) {
				event.setCancelled(true);
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_sign_already_exists_at_port", destinationTownName));
				return;
			}
			//Sign creation successful
			TownMetaDataController.addPortSign(sourceTown, event.getBlock(), destinationTown.getName());
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_success_fast_travel_sign_created", destinationTownName));

		}
	}

	@EventHandler(ignoreCancelled = true)
	public void on(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK  
				|| !event.hasBlock() 
				|| event.getClickedBlock() == null
				|| !FastTravelUtil.isFastTravelSign(event.getClickedBlock())) {
			return;
		}
		//Player clicked a fast travel sign. Lets see if we can move them
		Block block = event.getClickedBlock();
		Sign sign = (Sign) block.getState();
		TownBlock townBlock = TownyAPI.getInstance().getTownBlock(block.getLocation());
		if (townBlock == null) {
			//Sign is in wilderness
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_not_in_a_town"));
			return;
		}
		Town sourceTown = townBlock.getTownOrNull();
		if(sourceTown == null) {
			//Sign is not in a town
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_not_in_a_town"));
			return;
		}
		String townBlockTypeNameLowercase = townBlock.getTypeName().toLowerCase();
		if (
			//!townBlock.isHomeBlock()
			///&& !townBlockTypeNameLowercase.equals("outpost")
			!townBlockTypeNameLowercase.equals("port")
			&& !townBlockTypeNameLowercase.equals("jump-node")) {
			//Sign not in a travel node type of plot
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_bad_plot_type"));
			return;
		}
		String line3 = sign.getLine(2);
		Town destinationTown = TownyAPI.getInstance().getTown(line3);
		if (destinationTown == null) {
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_unknown_destination_town", line3));
			return;
		}
		//Check individual travel types
		if (townBlockTypeNameLowercase.equals("jump-node")) {
			if (!TownMetaDataController.hasJumpNode(destinationTown)) {
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_destination_town_has_no_jump_node", line3));
				return;
			}
			//Find the return sign at the destination
			Block returnSign = TownMetaDataController.getJumpNodeSigns(destinationTown).get(sourceTown.getName());
			if(returnSign == null) {
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_destination_plot_had_no_return_sign", line3));
				return;
			}
			//Jump now
			event.getPlayer().teleport(returnSign.getLocation());

		} else if (townBlockTypeNameLowercase.equals("port")) {
			if (!TownMetaDataController.hasPort(destinationTown)) {
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_destination_town_has_no_port", line3));
				return;
			}
			//Find the return sign at the destination
			Block returnSign = TownMetaDataController.getPortSigns(destinationTown).get(sourceTown.getName());
			if(returnSign == null) {
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_destination_plot_had_no_return_sign", line3));
				return;
			}
			//Check distance
			double actualDistance = MathUtil.distance(
					event.getClickedBlock().getX(),
					event.getClickedBlock().getZ(),
					returnSign.getX(),
					returnSign.getZ());
			double maxDistance = TownyProvincesSettings.getPortsMaxFastTravelRange();
			if(actualDistance > maxDistance) {
				Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_fast_travel_sign_did_not_work_destination_port_is_too_far_away", line3, (int)maxDistance, (int)actualDistance));
				return;
			}
			//Jump now
			event.getPlayer().teleport(returnSign.getLocation());
			event.setCancelled(true);
		}
	}

	/**
	 * If a player breaks a fast-travel-sign, delete the metadata
	 * @param event block break event
	 */
	@EventHandler(ignoreCancelled = true)
	public void on(BlockBreakEvent event) {
		if(!TownyAPI.getInstance().isWilderness(event.getBlock()) 
				&& FastTravelUtil.isFastTravelSign(event.getBlock())) {
			TownBlock townBlock = TownyAPI.getInstance().getTownBlock(event.getBlock().getLocation());
			if(townBlock == null)
				return;
			Town town = townBlock.getTownOrNull();
			if(town == null)
				return;
			TownBlockType townBlockType = townBlock.getType();
			if(townBlockType == null)
				return;
			if(townBlockType.getName().equalsIgnoreCase("jump-node")) {
				if(TownMetaDataController.hasJumpNode(town)
						&& TownMetaDataController.removeJumpNodeSign(town, event.getBlock())) {
					town.save();
				}
			} else if (townBlockType.getName().equalsIgnoreCase("port")) {
				if(TownMetaDataController.hasPort(town)
						&& TownMetaDataController.removePortSign(town, event.getBlock())) {
					town.save();
				}
			}
		}
	}

}
	

