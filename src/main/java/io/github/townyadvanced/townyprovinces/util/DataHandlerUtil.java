package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataHandlerUtil {
	private static final String dataFolderPath = "data";
	private static final String provincesFolderPath = "data/provinces";
	private static final String provinceBlocksFolderPath = "data/provinceblocks";


	public static boolean setupDataFoldersIfRequired() {
		try {
			FileUtil.setupFolderIfRequired(dataFolderPath);
			FileUtil.setupFolderIfRequired(provincesFolderPath);
			FileUtil.setupFolderIfRequired(provinceBlocksFolderPath);
		} catch (Exception e) {
			TownyProvinces.severe("Problem Setting up data folders");
		}
		return true;
	}
	
	public static boolean loadData() {
		loadProvinces();
		loadProvinceBlocks();
		return true; //Loaded but blank
	}

	public static void loadProvinces() {
		TownyProvinces.info("Now Loading Province Files");
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
		TownyProvinces.info("Province Files Loaded");
	}


	private static void loadProvinceBlocks() {
		TownyProvinces.info("Now Loading Province Block Files");
		List<File> provinceBlockFiles = FileUtil.readListOfFiles(provinceBlocksFolderPath);
		for(File provinceBlockFile: provinceBlockFiles) {
			//Load file
			Map<String,String> fileEntries = FileMgmt.loadFileIntoHashMap(provinceBlockFile);
			Coord coord = unpackCoord(fileEntries.get("coord"));
			UUID provinceUuid = UUID.fromString(fileEntries.get("province_uuid"));
			Province province = TownyProvincesDataHolder.getInstance().getProvince(provinceUuid);
			boolean border = Boolean.parseBoolean(fileEntries.get("border"));
			ProvinceBlock provinceBlock = new ProvinceBlock(coord, province, border);
			//Add province block to TP universe
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, provinceBlock);
		}
		TownyProvinces.info("Province Block Files Loaded");
	}


	private static Coord unpackCoord(String coordAsString) {
		String[] xz = coordAsString.split(",");
		int x = Integer.parseInt(xz[0]);
		int z = Integer.parseInt(xz[1]);
		return new Coord(x,z);
	}


	public void loadTPChunksMap() {
		
	}
	
	
}
