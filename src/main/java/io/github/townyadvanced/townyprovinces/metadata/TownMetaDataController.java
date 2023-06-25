package io.github.townyadvanced.townyprovinces.metadata;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;

/**
 * 
 * @author LlmDl
 *
 */
public class TownMetaDataController {

	@SuppressWarnings("unused")
	private static StringDataField portCoord = new StringDataField("townyprovinces_portCoord", "");

	@Nullable
	private static WorldCoord getPortCoord(Town town) {
		String portCoordString = getPortCoordAsString(town);
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
	private static String getPortCoordAsString(Town town) {
		StringDataField sdf = (StringDataField) portCoord.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}
	
}
