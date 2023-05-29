package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.util.FileMgmt;
import io.github.townyadvanced.townyprovinces.TownyProvinces;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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
}
