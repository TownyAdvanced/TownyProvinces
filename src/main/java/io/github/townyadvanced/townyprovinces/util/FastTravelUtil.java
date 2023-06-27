package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.townyprovinces.metadata.TownMetaDataController;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class FastTravelUtil {
	
	public static void removeAllTracesOfJumpNode(Town town) {
		//Break the fast travel signs
		for(Block block: TownMetaDataController.getJumpNodeSigns(town).values()) {
			block.breakNaturally();
		}
		//Remove metadata
		TownMetaDataController.removeJumpNodeCoord(town);
		TownMetaDataController.removeJumpNodeSigns(town);
		town.save();
	}

	public static void removeAllTracesOfPort(Town town) {
		//Break the fast travel signs
		for(Block block: TownMetaDataController.getPortSigns(town).values()) {
			block.breakNaturally();
		}
		//Remove metadata
		TownMetaDataController.removePortCoord(town);
		TownMetaDataController.removePortSigns(town);
		town.save();
	}
	
	public static boolean isFastTravelSign(Block block) {
		return block.getState() instanceof Sign
			&& (((Sign) block.getState()).getLine(0).trim().equals(">>>"));
	}
}