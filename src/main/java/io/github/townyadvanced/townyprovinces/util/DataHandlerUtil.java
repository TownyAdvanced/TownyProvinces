package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataHandlerUtil {
	public static final String dataFolderPath = "data";
	public static final String provinceGeneratorsFolderPath = "province_generators";
	public static final String provincesFolderPath = "data/provinces";
	public static final String provinceBlocksFolderPath = "data/province_blocks";
	
	public static boolean setupPluginSubFoldersIfRequired() {
		try {
			FileUtil.setupFolderIfRequired(dataFolderPath);
			FileUtil.setupFolderIfRequired(provincesFolderPath);
			FileUtil.setupFolderIfRequired(provinceBlocksFolderPath);
			FileUtil.setupFolderIfRequired(provinceGeneratorsFolderPath);
		} catch (Exception e) {
			TownyProvinces.severe("Problem Setting up plugin sub-folders");
		}
		return true;
	}
	
	public static boolean loadAllData() {
		loadProvinces();
		loadProvinceBlocks();
		return true; 
	}

	public static boolean saveAllData() {
		saveProvinces();
		saveProvinceBlocks();
		return true; 
	}


	private static void saveProvinces() {
		TownyProvinces.info("Now Saving Provinces");
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(provincesFolderPath).toString();

		//Delete existing files
		List<File> provinceFiles = FileUtil.readListOfFiles(provincesFolderPath);
		for(File file: provinceFiles) {
			file.delete();
		}
		
		//Save all province files
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			String fileName = folderPath + "/province_" + province.getUuid().toString() + ".yml";
			Map<String,String> fileEntries = new HashMap<>(); 
			fileEntries.put("home_block", "" + province.getHomeBlock().getX() + "," + province.getHomeBlock().getZ());
			fileEntries.put("uuid", "" + province.getUuid().toString());
			fileEntries.put("new_town_price", "" + province.getNewTownPrice());
			fileEntries.put("town_upkeep", "" + province.getTownUpkeep());
			FileUtil.saveHashMapIntoFile(fileEntries, fileName);
		}
		TownyProvinces.info("Provinces Saved");
	}

	private static void loadProvinces() {
		TownyProvinces.info("Now Loading Provinces");
		List<File> provinceFiles = FileUtil.readListOfFiles(provincesFolderPath);
		for(File provinceFile: provinceFiles) {
			//Load province file
			Map<String,String> fileEntries = FileMgmt.loadFileIntoHashMap(provinceFile);
			Coord homeBlock = unpackCoord(fileEntries.get("home_block"));
			UUID uuid = UUID.fromString(fileEntries.get("uuid"));
			Province province = new Province(homeBlock, uuid);
			province.setNewTownPrice(Integer.parseInt(fileEntries.get("new_town_price")));
			province.setTownUpkeep(Integer.parseInt(fileEntries.get("town_upkeep")));
			//Add province to TP universe
			TownyProvincesDataHolder.getInstance().addProvince(province);
		}
		TownyProvinces.info("Provinces Loaded");
	}

	private static void saveProvinceBlocks() {
		TownyProvinces.info("Now Saving Province Blocks");
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(provinceBlocksFolderPath).toString();
		
		//Delete existing files
		List<File> provinceBlockFiles = FileUtil.readListOfFiles(provinceBlocksFolderPath);
		for(File file: provinceBlockFiles) {
			file.delete();
		}

		//Save all province block files
		for(Map.Entry<Coord,ProvinceBlock> provinceBlockEntry: TownyProvincesDataHolder.getInstance().getProvinceBlocks().entrySet()) {
			//Make file name
			Coord coord = provinceBlockEntry.getKey();
			ProvinceBlock provinceBlock = provinceBlockEntry.getValue();
			String fileName = folderPath + "/province_block_x" + coord.getX() + "_z" + coord.getZ() + ".yml";
			//Make content
			Map<String,String> fileEntries = new HashMap<>();
			fileEntries.put("coord", "" + coord.getX() + "," + coord.getZ());
			fileEntries.put("province_uuid", provinceBlock.getProvince() == null ? "" : provinceBlock.getProvince().getUuid().toString());
			fileEntries.put("border", "" + provinceBlock.isProvinceBorder());
			FileUtil.saveHashMapIntoFile(fileEntries, fileName);
		}
		TownyProvinces.info("Province Blocks Saved");
	}

	private static void loadProvinceBlocks() {
		TownyProvinces.info("Now Loading Province Blocks");
		List<File> provinceBlockFiles = FileUtil.readListOfFiles(provinceBlocksFolderPath);
		for(File provinceBlockFile: provinceBlockFiles) {
			//Load file
			Map<String,String> fileEntries = FileMgmt.loadFileIntoHashMap(provinceBlockFile);
			Coord coord = unpackCoord(fileEntries.get("coord"));
			boolean border = Boolean.parseBoolean(fileEntries.get("border"));
			Province province;
			if(border) {
				province = null;
			} else {
				UUID provinceUuid = UUID.fromString(fileEntries.get("province_uuid"));
				province = TownyProvincesDataHolder.getInstance().getProvince(provinceUuid);
			}
			//Create province block
			ProvinceBlock provinceBlock = new ProvinceBlock(coord, province, border);
			//Add province block to TP universe
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, provinceBlock);
		}
		TownyProvinces.info("Province Blocks Loaded");
	}


	private static Coord unpackCoord(String coordAsString) {
		String[] xz = coordAsString.split(",");
		int x = Integer.parseInt(xz[0]);
		int z = Integer.parseInt(xz[1]);
		return new Coord(x,z);
	}

	
}
