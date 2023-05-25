package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProvinceCreatorUtil {

	/**
	 * Create all provinces in the world
	 */
	public static boolean createProvinces() {
	
		//Create province objects - empty except for the homeblocks
		if(!createProvinceObjects()) {
			return false;
		}
		
		//Claim all chunks in the target area (except isolated Ocean maybe)
		if(!claimAllChunksForProvinces()) {
			return false;
		}

		//Setup province borders
		if(!setupProvinceBorders()) {
			return false;
		}


		TownyProvinces.info("Provinces Created: " + TownyProvincesDataHolder.getInstance().getNumProvinces());
		return true;
	}

	private static boolean setupProvinceBorders() {
		//Process all provinces, starting with the ones which have the least amount of processed neighbors
		Set<Province> allProvinces = new HashSet<>(TownyProvincesDataHolder.getInstance().getProvinces());
		Set<Province> unProcessedProvinces = new HashSet<>(allProvinces);
		Set<Province> processedProvinces = new HashSet<>();
		while(unProcessedProvinces.size() > 0) {
			TownyProvinces.info("PROC_PROVINCES: " + processedProvinces.size());
			TownyProvinces.info("UNPROC_PROVINCES: " + unProcessedProvinces.size());
			Province province = findUnProcessedProvinceWithLeastAmountOfProcessedNeighbors(unProcessedProvinces, processedProvinces);
			setupProvinceBorders(province);
			unProcessedProvinces.remove(province);
			processedProvinces.add(province);
		}
		
		TownyProvinces.info("PROVINCES PROCESSED: " + processedProvinces.size());
		return true;
	}

	private static Province findUnProcessedProvinceWithLeastAmountOfProcessedNeighbors(Set<Province> unProcessedProvinces, Set<Province> processedProvinces) {
		Province winner = null;
		int winnerNumberOfProcessedNeighbors =  999999999;
		for(Province candidate: unProcessedProvinces) {
			int candidateNumberOfProcessedNeighbors = calculateNumberOfProcessedNeighbors(candidate, processedProvinces);
			if(winner == null || candidateNumberOfProcessedNeighbors < winnerNumberOfProcessedNeighbors) {
				winner = candidate;
				winnerNumberOfProcessedNeighbors = candidateNumberOfProcessedNeighbors;
			}
		}
		return winner;
	}

	private static int calculateNumberOfProcessedNeighbors(Province province, Set<Province> processedProvinces) {
		int result = 0;
		Set<Province> neighboringProvinces = calculateNeigboringProvinces(province);
		for(Province neighborProvince: neighboringProvinces) {
			if(processedProvinces.contains(neighborProvince)) {
				result++;
			}
		}
		return result;
	}
	
	public static Set<Province> calculateNeigboringProvinces(Province province) {
		Set<Province> result = new HashSet<>();
		for(ProvinceBlock provinceBlock: province.getProvinceBlocks()) {
			result.addAll(calculateNeigboringProvinces(provinceBlock));
		}
		return result;
	}

	public static Set<Province> calculateNeigboringProvinces(ProvinceBlock givenProvinceBlock) {
		Coord givenProvinceBlockCoord = givenProvinceBlock.getCoord();
		Province givenProvince = givenProvinceBlock.getProvince();
		Coord adjacentCoord;
		ProvinceBlock adjacentProvinceBlock;
		Set<Province> result = new HashSet<>();
		for(int z = -1; z <=1; z++) {
			for(int x = -1; x <=1; x++) {
				if(x == 0 && z == 0) {
					continue;
				}
				adjacentCoord = new Coord(givenProvinceBlockCoord.getX() + x, givenProvinceBlockCoord.getZ() + z);
				adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if (adjacentProvinceBlock != null && adjacentProvinceBlock.getProvince() != givenProvince) {
					result.add(adjacentProvinceBlock.getProvince());
				}
			}
		}
		return result;
	}



	private static boolean setupProvinceBorders(Province province) {
		for (ProvinceBlock provinceBlock : province.getProvinceBlocks()) {
			if (shouldThisProvinceBlockBeAProvinceBorder(provinceBlock)) {
				provinceBlock.setProvinceBorder(true);
				//provinceBlock.setProvince(null); //Todo - probably remove this line
			}
		}
		return true;
	}
	
	private static boolean shouldThisProvinceBlockBeAProvinceBorder(ProvinceBlock provinceBlock) {
		Coord provinceBlockCoord = provinceBlock.getCoord();
		Province province = provinceBlock.getProvince();
		Coord adjacentCoord;
		ProvinceBlock adjacentProvinceBlock;
		for(int z = -1; z <=1; z++) {
			for(int x = -1; x <=1; x++) {
				adjacentCoord = new Coord(provinceBlockCoord.getX() + x, provinceBlockCoord.getZ() + z);
				adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if(adjacentProvinceBlock == null) {
					return true;
				} else if (adjacentProvinceBlock.getProvince() != province && !adjacentProvinceBlock.isProvinceBorder()) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean claimAllChunksForProvinces() {
		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, 8));
		}
		
		//Do claim competition
		//Loop each brush x times //TODO parameterize num loops
		for(int i = 0; i < 200; i++) {
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//Claim chunks at current position
				claimChunksIfPossible(provinceClaimBrush);				
				
				//Move brush
				//Todo - parameterize the move amount
				int moveAmountX = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				int moveAmountZ = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				Coord destination = new Coord(provinceClaimBrush.getCurrentPosition().getX() + moveAmountX, provinceClaimBrush.getCurrentPosition().getZ() + moveAmountZ);
				moveBrushIfPossible(provinceClaimBrush, destination);
			}
		}
		
		if(TownyProvincesDataHolder.getInstance().getProvinceBlocks().size() > 0) {
			claimRemainingNonBorderChunks();
		} else {
			TownyProvinces.severe("Unamble to claim remaining chunks as there are no claimed chunks");
			return false;
		}

		
		TownyProvinces.info("Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().values().size());
		return true;
	}

	private static void claimRemainingNonBorderChunks() {

		//Create claim queue
		List<Coord> claimQueue = new ArrayList<>();
		int minX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Coord coord;
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				coord = new Coord(x,z);
				ProvinceBlock provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock == null) {
					claimQueue.add(coord);
				}
			}
		}
		
		//Process claim queue
		while(true) {
			if(processClaimQueue(claimQueue)) {
				break;
			}
		}
	}

	/**
	 * 
	 * @param claimQueue
	 * @return true if the queue was cleared
	 */
	private static boolean processClaimQueue(List<Coord> claimQueue) {
		TownyProvinces.info("Claim Queue Size: " + claimQueue.size());
		
		List<Coord> copyOfClaimQueue = new ArrayList<>(claimQueue);
		
		Province adjacentProvince;
		for(Coord coord: copyOfClaimQueue) {
			adjacentProvince = getAdjacentProvince(coord);
			if(adjacentProvince != null) {
				//Claim chunk and remove coord from queue
				ProvinceBlock provinceBlock = new ProvinceBlock();
				provinceBlock.setProvince(adjacentProvince);
				provinceBlock.setCoord(coord);
				TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, provinceBlock);
				claimQueue.remove(coord);
				TownyProvinces.info("Chunk Claimed");
			} else {
				//Send coord to back of queue
				claimQueue.remove(coord);
				claimQueue.add(coord);
			}
		}
		return claimQueue.size() == 0;
	}

	private static @Nullable Province getAdjacentProvince(Coord unclaimedChunkCoord) {
		Coord adjacentCoord;
		ProvinceBlock provinceBlock;
		for(int x = unclaimedChunkCoord.getX() -1; x <= unclaimedChunkCoord.getX() + 1; x++) {
			for (int z = unclaimedChunkCoord.getZ() - 1; z <= unclaimedChunkCoord.getZ() + 1; z++) {
				adjacentCoord = new Coord(x, z);
				if (adjacentCoord.equals(unclaimedChunkCoord)) {
					continue;
				}
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
				if (provinceBlock != null) {
					return provinceBlock.getProvince();	
				}
			}
		}
		return null;
	}


	//Don't move if any of the brush would overlap another province
	private static void moveBrushIfPossible(ProvinceClaimBrush brush, Coord destination) {
		int startX = destination.getX() - brush.getSquareRadius();
		int endX = destination.getX() + brush.getSquareRadius();
		int startZ = destination.getZ() - brush.getSquareRadius();
		int endZ = destination.getZ() + brush.getSquareRadius();

		Coord coord;
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				coord = new Coord(x,z);
				ProvinceBlock provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock != null && provinceBlock.getProvince() != brush.getProvince()) {
					return; //Can't move, different province
				}
			}
		}

		brush.moveBrush(destination);
	}

	private static void claimChunkForNearestProvince(Coord chunkCoord) {
		Province nearestProvince = null;
		double nearestProvinceDistance = 9999999999999999d;
		double candidateProvinceDistance;
		for(Province province: TownyProvincesDataHolder.getInstance().getProvinces()) {
			candidateProvinceDistance = MathUtil.distance(chunkCoord, province.getHomeBlock());
			if(candidateProvinceDistance < nearestProvinceDistance) {
				nearestProvince = province;
				nearestProvinceDistance = candidateProvinceDistance;
			}
		}
		ProvinceBlock provinceBlock = new ProvinceBlock();
		provinceBlock.setProvince(nearestProvince);
		provinceBlock.setCoord(chunkCoord);
		TownyProvincesDataHolder.getInstance().addProvinceBlock(chunkCoord, provinceBlock);
	}


	private static void claimChunksIfPossible(ProvinceClaimBrush brush) {
		int startX = brush.getCurrentPosition().getX() - brush.getSquareRadius();
		int endX = brush.getCurrentPosition().getX() + brush.getSquareRadius();
		int startZ = brush.getCurrentPosition().getZ() - brush.getSquareRadius();
		int endZ = brush.getCurrentPosition().getZ() + brush.getSquareRadius();

		Coord coord;
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//return if this would claim over another province
				coord = new Coord(x,z);
				ProvinceBlock provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock != null && provinceBlock.getProvince() != provinceBlock.getProvince())
					return;
			}
		}

		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//Claim chunk if possible
				claimChunkIfPossible(new Coord(x,z), brush.getProvince());
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or an ocean chunk
	 * @param coord co-ord of chunk
	 * @param province the province doing the claiming
	 */
	private static void claimChunkIfPossible(Coord coord, Province province) {
		if (TownyProvincesDataHolder.getInstance().getProvinceBlock(coord) != null)
			return;

		int minX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		
		if(coord.getX() < minX)
			return;
		else if (coord.getX() > maxX)
			return;
		else if (coord.getZ() < minZ)
			return;
		else if (coord.getZ() > maxZ)
			return;
		
		ProvinceBlock provinceBlock = new ProvinceBlock();
		provinceBlock.setProvince(province);
		provinceBlock.setCoord(coord);
		TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, provinceBlock);
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
		double tpChunkSideLength = TownyProvincesSettings.getProvinceBlockSideLength();
		for(int i = 0; i < 100; i++) {
			double xLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX();
			double xHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX();
			double zLowest = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ();
			double zHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ();
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / tpChunkSideLength);
			int zCoord = (int)(z / tpChunkSideLength);
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
		int minAllowedDistanceInChunks = minAllowedDistanceInMetres / TownyProvincesSettings.getProvinceBlockSideLength();
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

 