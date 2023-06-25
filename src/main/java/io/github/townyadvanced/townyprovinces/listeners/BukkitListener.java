package io.github.townyadvanced.townyprovinces.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.hooks.PluginIntegrations;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.townyprovinces.messaging.Messaging;
import io.github.townyadvanced.townyprovinces.metadata.TownMetaDataController;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * 
 * @author LlmDl
 *
 */
public class BukkitListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onSignChangeEvent(SignChangeEvent event) {
		if (!TownyProvincesSettings.isTownyProvincesEnabled()) {
			return;
		}
		if (TownyProvincesSettings.isJumpNodesEnabled()) {
			evaluateSignChangeEventForJumpNode(event);
		}
	}

	private void evaluateSignChangeEventForJumpNode(SignChangeEvent event) {
		//Is this on a jump node
		if (TownyAPI.getInstance().isWilderness(event.getBlock()))
			return;
		TownBlock townBlock = TownyAPI.getInstance().getTownBlock(event.getBlock().getLocation());
		if (!townBlock.getTypeName().equalsIgnoreCase("jump-node"))
			return;
		//Ok now we know we are setting the text on a sign in a jump node
		if (event.getLines().length == 0)
			return; //Probably won't happen
		String line1 = event.getLine(0);
		if (line1 == null || !line1.trim().equals(">>>"))
			return; //Not a fast travel sign
		String line2 = event.getLine(1);
		if (line2 == null || !line2.trim().equalsIgnoreCase("fast travel")) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_incorrect_configuration"));
			return;
		}
		String line3 = event.getLine(2);
		if (line3 == null || !line3.trim().equalsIgnoreCase("to")) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_incorrect_configuration"));
			return;
		}
		String line4 = event.getLine(3);
		if (line4 == null || !(line4.length() == 0)) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_incorrect_configuration"));
			return;
		}
		Town destinationTown = TownyAPI.getInstance().getTown(line4.trim());
		if (destinationTown == null) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign_unknown_destination_town", line4.trim()));
			return;
		}
		boolean destinationHasJumpHub = TownMetaDataController.hasJumpHub(destinationTown);
		if (!destinationHasJumpHub) {
			event.setCancelled(true);
			Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_err_cannot_create_fast_travel_sign__no_jump_hub_at_destination_town", line4.trim()));
			return;
		}
		//Sign creation successful
		Messaging.sendMsg(event.getPlayer(), Translatable.of("msg_success_fast_travel_sign_created", line4.trim()));
	}
}
	

