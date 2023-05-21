package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import java.util.List;

public class ProvinceCreatorUtil {

	/**
	 * Create all provinces in the world
	 */
	public static boolean createProvinces() {
	
		//Create province objects - empty except for the homeblocks
		if(!createProvinceObjects()) {
			return false;
		}
		
		//Fill each province wth chunks
		
		TownyProvinces.info("Provinces Created: " + TownyProvincesDataHolder.getInstance().getNumProvinces());
		return true;
	}

	/**
	 * Create province object - empty except for the homeblocks
	 * 
	 * @return false if we failed to create sufficient provinces
	 */
	private static boolean createProvinceObjects() {
		Coord provinceHomeBlock;
		int idealNumberOfProvinces = calculateIdealNumberOfProvinces();
		for (int provinceIndex = 0; provinceIndex < idealNumberOfProvinces; provinceIndex++) {
			provinceHomeBlock = generateProvinceHomeBlock();
			if(provinceHomeBlock != null) { 
				//Province homeblock generated. Now create province
				Province province = new Province();
				province.setHomeBlock(provinceHomeBlock);
				TownyProvincesDataHolder.getInstance().addProvince(province);
			} else {
				//Could not generate a province homeblock
				double allowedVariance = TownyProvincesSettings.getMaxAllowedVarianceBetweenIdealAndActualNumProvinces();
				double minimumAllowedNumProvinces = ((double) idealNumberOfProvinces) * (1 - allowedVariance);
				int actualNumProvinces = TownyProvincesDataHolder.getInstance().getNumProvinces();
				if (actualNumProvinces < minimumAllowedNumProvinces) {
					TownyProvinces.severe("ERROR: Could not create the minimum number of provinces. Required: " + minimumAllowedNumProvinces + ". Actual: " + actualNumProvinces);
					return false;
				} else {
					TownyProvinces.info("" + actualNumProvinces + " province objects created, each one containing just homeblock info.");
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * Generate a new province homeBlock
	 * Return null if you fail - usually due to map being full up with provinces
	 */
	private static Coord generateProvinceHomeBlock() {
		double townBlockSize = TownySettings.getTownBlockSize();
		for(int i = 0; i < 100; i++) {
			double xLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX();
			double xHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX();
			double zLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ();
			double zHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ();
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / townBlockSize);
			int zCoord = (int)(z / townBlockSize);
			Coord generatedHomeBlockCoord = new Coord(xCoord, zCoord);
			if(validateProvinceHomeBlock(generatedHomeBlockCoord)) {
				TownyProvinces.info("Province homeblock generated");
				return generatedHomeBlockCoord;
			}
		}
		TownyProvinces.info("Could not generate province homeblock");
		return null;
	}
	
	private static boolean validateProvinceHomeBlock(Coord coord) {
		int minAllowedDistanceInMetres = TownyProvincesSettings.getMinAllowedDistanceBetweenProvinceHomeBlocks();
		int minAllowedDistanceInChunks = minAllowedDistanceInMetres / TownySettings.getTownBlockSize();
		List<Province> provinceList = TownyProvincesDataHolder.getInstance().getProvinces();
		for(Province province: provinceList) {
			if(MathUtil.distance(coord, province.getHomeBlock()) < minAllowedDistanceInChunks) {
				return false;
			}
		}
		return true;
	}
	
	private static int calculateIdealNumberOfProvinces() {
		double worldAreaSquareMetres = calculateWorldAreaSquareMetres();
		double averageProvinceAreaSquareMetres = TownyProvincesSettings.getAverageProvinceSizeInSquareMetres();
		int idealNumberOfProvinces = (int)(worldAreaSquareMetres / averageProvinceAreaSquareMetres);
		TownyProvinces.info("Ideal num provinces: " + idealNumberOfProvinces);
		return idealNumberOfProvinces;
	}
	
	private static double calculateWorldAreaSquareMetres() {
		Location topLeftCorner = TownyProvincesSettings.getTopLeftWorldCornerLocation();
		Location bottomRightCorner = TownyProvincesSettings.getBottomRightWorldCornerLocation();
		double sideLengthX = Math.abs(topLeftCorner.getX() - bottomRightCorner.getX());
		double sideLengthZ = Math.abs(topLeftCorner.getZ() - bottomRightCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;

	}
}

 