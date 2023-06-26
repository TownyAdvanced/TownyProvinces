package io.github.townyadvanced.townyprovinces.metadata;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TownMetaDataController {
	
	private static final StringDataField jumpHubCoord = new StringDataField("townyprovinces_jumpNodeCoord", "");

	private static final StringDataField jumpHubSigns = new StringDataField("townyprovinces_jumpHubSigns", "");



	//["towny_stringdf","townyprovinces_signs","{world,-3555,-10333,lerglon},{world,-3675,-13545,prepdo}"]
	
	
	@Nullable
	public static boolean hasJumpHub(Town town) {
		String portCoordString = getJumpNodeCoordAsString(town);
		return portCoordString != null;
	}

	@Nullable
	public static WorldCoord getJumpHubWorldCoord(Town town) {
		String portCoordString = getJumpNodeCoordAsString(town);
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
	private static String getJumpNodeCoordAsString(Town town) {
		StringDataField sdf = (StringDataField) jumpHubCoord.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}
	
	public static void setJumpNodeCoord(Town town, WorldCoord worldCoord) {
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
	
	public static Map<String, Block> getJumpHubSigns(Town town) {
		Map<String, Block> result = new HashMap<>();
		String signsAsString = getJumpHubSignsAsString(town);
		if(signsAsString != null && signsAsString.length() > 0) {
			String[] signsAsArray = signsAsString.split("\\|");
			String[] signAsArray;
			World world;
			int x;
			int y;
			int z;
			String destinationTownName;
			Block block;
			for(String signAsString: signsAsArray) {
				signAsArray = signAsString.split(",");
				world = Bukkit.getWorld(signAsArray[0]);
				x = Integer.parseInt(signAsArray[1]);
				y = Integer.parseInt(signAsArray[2]);
				z = Integer.parseInt(signAsArray[3]);
				destinationTownName = signAsArray[4];
				block = (new Location(world,x,y,z)).getBlock();
				result.put(destinationTownName, block);
			}
		}
		return result;
	}

	public static void addJumpHubSign(Town town, Block signBlock, String destinationTownName) {
		StringBuilder builder;
		String value = getJumpHubSignsAsString(town);
		//Remove data
		if(value != null) {
			removeJumpHubSigns(town);
			builder = new StringBuilder(value);
			builder.append("|");
		} else {
			builder = new StringBuilder();
		}
		//Add the new values to the builder
		builder.append(signBlock.getWorld().getName());
		builder.append(",");
		builder.append(signBlock.getX());
		builder.append(",");
		builder.append(signBlock.getY());
		builder.append(",");
		builder.append(signBlock.getZ());
		builder.append(",");
		builder.append(destinationTownName);
		//Set the required metadata field
		StringDataField sdf = (StringDataField) jumpHubSigns.clone();
		if (town.hasMeta(sdf.getKey()))
			MetaDataUtil.setString(town, sdf, builder.toString(), true);
		else
			town.addMetaData(new StringDataField("townyprovinces_jumpHubSigns", builder.toString()));
	}
	
	/**
	 *
	 * worldName,x,y,z,destinationTown| 
	 */
	@Nullable
	private static String getJumpHubSignsAsString(Town town) {
		StringDataField sdf = (StringDataField) jumpHubSigns.clone();
		if (town.hasMeta(sdf.getKey())) {
			return MetaDataUtil.getString(town, sdf);
		}
		return null;
	}

	public static void removeJumpHubSigns(Town town) {
		town.removeMetaData(jumpHubSigns.clone());
	}

	/**
	 * Remove the sign IF it is in the metadata
	 * @param block the sign
	 */
	public static void removeJumpHubSign(Town town, Block block) {
		Map<String, Block> jumpHubSigns = getJumpHubSigns(town);
		String signKey = null;
		for(Map.Entry<String,Block> mapEntry: jumpHubSigns.entrySet()) {
			if(mapEntry.getValue().equals(block)) {
				signKey = mapEntry.getKey();
				break;
			}
		}
		if(signKey != null) {
			jumpHubSigns.remove(signKey);
			setJumpHubSigns(town, jumpHubSigns);
		}
	}

	public static void setJumpHubSigns(Town town, Map<String,Block> newJumpHubSigns) {
		removeJumpHubSigns(town);
		for(Map.Entry<String,Block> mapEntry: newJumpHubSigns.entrySet()) {
			addJumpHubSign(town, mapEntry.getValue(), mapEntry.getKey());
		}
	}
	
}
