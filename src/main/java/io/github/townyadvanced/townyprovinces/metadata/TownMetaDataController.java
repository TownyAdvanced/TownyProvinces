package io.github.townyadvanced.townyprovinces.metadata;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;

public class TownMetaDataController {

	@SuppressWarnings("unused")
	private static StringDataField jumpHubCoord = new StringDataField("townyprovinces_jumpNodeCoord", "");

	@Nullable
	public static boolean hasJumpHub(Town town) {
		String portCoordString = getJumpHubCoordAsString(town);
		return portCoordString != null;
	}

	@Nullable
	public static WorldCoord getJumpHubCoord(Town town) {
		String portCoordString = getJumpHubCoordAsString(town);
		if(portCoordString == null)
			return null;
		String[] worldCoordArray = portCoordString.split(",");
		World world = Bukkit.getWorld(worldCoordArray[0]);
		int x = Integer.parseInt(worldCoordArray[1]);
		int z = Integer.parseInt(worldCoordArray[2]);
		WorldCoord portCoord = new WorldCoord(world, x, z);
		return portCoord;
	}

	@Nullable
	private static String getJumpHubCoordAsString(Town town) {
		StringDataField sdf = (StringDataField) jumpHubCoord.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}
	
	public static void addJumpHubCoord(Town town, WorldCoord worldCoord) {
		int x = worldCoord.getX();
		int z = worldCoord.getZ();
		String worldName = worldCoord.getWorldName();
		String metadataValue = worldName + "," + x + "," + z;
		StringDataField sdf = (StringDataField) jumpHubCoord.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, metadataValue, true);
		else
			town.addMetaData(new StringDataField("townyprovinces_jumpNodeCoord", metadataValue));
	}

	public static void removeJumpHubCoord(Town town) {
		town.removeMetaData(jumpHubCoord.clone());
	}
}
