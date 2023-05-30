package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileUtil {
	public static void setupFolderIfRequired(String relativeFolderPath) {
		Path folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath);
		FileMgmt.checkOrCreateFolder(folderPath.toString());
	}

	public static List<File> readListOfFiles(String relativeFolderPath) {
		String folderPath = TownyProvinces.getPlugin().getDataFolder().toPath().resolve(relativeFolderPath).toString();
		File folder = new File(folderPath);
		return Arrays.asList(Objects.requireNonNull(folder.listFiles()));
	}

	public static void saveHashMapIntoFile(Map<String, String> fileEntries, String filePath) {
		try {
			List<String> linesToWrite = transformFileEntriesMapToListOfLines(fileEntries);
			File file = new File(filePath);
			file.createNewFile();
			FileMgmt.listToFile(linesToWrite, filePath);
		} catch (IOException e) {
			String provinceId = fileEntries.getOrDefault("uuid", "?");
			String provinceHomeBlock = fileEntries.getOrDefault("home_block", "?"); 
			TownyProvinces.severe("Problem Saving Province To File. Province ID: " + provinceId + ". Province HomeBlock: " + provinceHomeBlock + ". Error: " + e.getMessage());
		}
	}

	private static List<String> transformFileEntriesMapToListOfLines(Map<String, String> fileEntriesMap) {
		List<String> result = new ArrayList<>();
		for(Map.Entry<String,String> mapEntry: fileEntriesMap.entrySet()) {
			result.add(mapEntry.getKey() + ": " + mapEntry.getValue());
		}
		return result;
	}

	public static List<File> readProvinceGeneratorFiles() {
		return readListOfFiles(DataHandlerUtil.provinceGeneratorsFolderPath);
	}
}
