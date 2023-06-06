package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProvinceGeneratorUtil {
	
	/**
	 * Generate all provinces in the world
	 */
	public static boolean generateProvinces() {
		List<File> regionDefinitionFiles = FileUtil.readRegionDefinitionFiles();
		Collections.sort(regionDefinitionFiles);
		
		//Paint all Provinces
		for (File regionDefinitionFile : regionDefinitionFiles) {
			if(!paintProvincesInRegion(regionDefinitionFile)) {
				return false;
			}
		}
		
		//Allocate unclaimed chunks to provinces.
		TownyProvincesSettings.setProvinceGenerationInstructions(regionDefinitionFiles.get(0));
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}
		
		//Cull ocean provinces
		Bukkit.getScheduler().runTaskAsynchronously(TownyProvinces.getPlugin(), new CullOceanProvincesTask() );
		return true;
	}
	
	private static boolean paintProvincesInRegion(File regionDefinitionFile) {
		TownyProvinces.info("Now Painting Provinces In Region: " + regionDefinitionFile.getName());

		//Setup settings with correct instructions
		TownyProvincesSettings.setProvinceGenerationInstructions(regionDefinitionFile);

		//Delete existing provinces which are mostly in the given area
		if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea()) {
			return false;
		}
		
		//Create province objects - empty except for the homeblocks
		if(!createProvinceHomeBlocks()) {
			return false;
		}
		
		//Execute chunk claim competition
		if(!executeChunkClaimCompetition()) {
			return false;
		}

		TownyProvinces.info("Finished Painting Provinces In Region: " + regionDefinitionFile.getName());
		return true;
	}

	private static boolean deleteExistingProvincesWhichAreMostlyInSpecifiedArea() {
		TownyProvinces.info("Now deleting provinces which are mostly in the specified area.");
		int numProvincesDeleted = 0;
		int minX = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			List<Coord> coordsInProvince = province.getCoordsInProvince();
			int numProvinceBlocksInSpecifiedArea = 0;
			for (Coord coordInProvince : coordsInProvince) {
				if (coordInProvince.getX() < minX)
					continue;
				else if (coordInProvince.getX() > maxX)
					continue;
				else if (coordInProvince.getZ() < minZ)
					continue;
				else if (coordInProvince.getZ() > maxZ)
					continue;
				numProvinceBlocksInSpecifiedArea++;
			}
			if(numProvinceBlocksInSpecifiedArea > (coordsInProvince.size() / 2)) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
				numProvincesDeleted++;
			}
		}
		TownyProvinces.info("" + numProvincesDeleted + " provinces deleted.");
		return true;
	}

	/**
	 * Cull provinces which are mainly ocean
	 * <p>
	 * This method will not always work perfectly
	 * because it checks the biomes just on the border,
	 * to avoid potentially hours of biome checking per world generation
	 * <p>
	 * Mistakes are expected,
	 * which is why server owners can run /tp province undelete
	 *
	 * @return true if there's no error
	 */
	private static class CullOceanProvincesTask implements Runnable {
		@Override
		public void run() {
			TownyProvinces.info("Now Deleting Ocean Provinces.");
			double numProvincesProcessed = 0;
			List<Province> provinces = TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList();
			for(Province province: provinces) {
				if(!province.isSea() && isProvinceMainlyOcean(province)) {
					province.setSea(true);
				}
				numProvincesProcessed ++;
				int percentCompletion = (int)((numProvincesProcessed / provinces.size()) * 100); 
				TownyProvinces.info("Ocean Province Deletion Job Progress: " +percentCompletion + "%");
			}
			TownyProvinces.info("Finished Deleting Ocean Provinces.");
		}
	}

	private static boolean isProvinceMainlyOcean(Province province) {
		List<Coord> coordsInProvince = province.getCoordsInProvince();
		String worldName = TownyProvincesSettings.getWorldName();
		World world = Bukkit.getWorld(worldName);
		Biome biome;
		Coord coordToTest;
		for(int i = 0; i < 10; i++) {
			coordToTest = coordsInProvince.get((int)(Math.random() * coordsInProvince.size()));
			int x = (coordToTest.getX() * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
			int z = (coordToTest.getZ() * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
			biome = world.getHighestBlockAt(x,z).getBiome();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.gc();
			if(!biome.name().toLowerCase().contains("ocean") && !biome.name().toLowerCase().contains("beach")) {
				return false;
			}
		}
		return true;
	}
	
	private static Set<Coord> findAllUnclaimedCoords() {
		Set<Coord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Coord coord;
		Province province;
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				coord = new Coord(x,z);
				province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
				if(province == null) {
					//This means the chunk is unclaimed
					result.add(coord);
				}
			}
		}
		return result;
	}
	
	/**
	 * Assign unclaimed chunks to provinces where possible
	 * 
	 * @return true on method success
	 */
	private static boolean assignUnclaimedChunksToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces.");
		List<Coord> coordsEligibleForProvinceAssignment;
		Province province;
		Set<Coord> unclaimedCoords = findAllUnclaimedCoords();
		while((coordsEligibleForProvinceAssignment = findCoordsEligibleForProvinceAssignment(unclaimedCoords)).size() > 0) {
			TownyProvinces.info("Num Unclaimed Chunks: " + unclaimedCoords.size());
			/*
			 * Cycle each eligible coord
			 * Assign it if it is still eligible when we get to it
			 */
			for(Coord coord: coordsEligibleForProvinceAssignment) {
				if((province = getProvinceIfCoordIsEligibleForProvinceAssignment(coord)) != null) {
					TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, province);
					unclaimedCoords.remove(coord);
				}
			}
		}
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}
	
	private static List<Coord> findCoordsEligibleForProvinceAssignment(Set<Coord> allUnclaimedCoords) {
		List<Coord> result = new ArrayList<>();

		Province province;
		for (Coord candidateCoord : allUnclaimedCoords) {
			province = getProvinceIfCoordIsEligibleForProvinceAssignment(candidateCoord);
			if(province != null) {
				result.add(candidateCoord);
			}
		}
		return result;

	}
	
	private static Province getProvinceIfCoordIsEligibleForProvinceAssignment(Coord candidateCoord) {
		int minX = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();

		//Filter out chunk if it is at edge of map
		if(candidateCoord.getX() <= minX)
			return null;
		else if (candidateCoord.getX() >= maxX)
			return null;
		else if (candidateCoord.getZ() <= minZ)
			return null;
		else if (candidateCoord.getZ() >= maxZ)
			return null;

		//Filter out chunk if it does not have exactly 1 adjacent province
		List<Province> adjacentProvinces = findAllAdjacentProvinces(candidateCoord);
		if(adjacentProvinces.size() != 1)
			return null;

		//Filter out chunk if the adjacent province is NOT on a cardinal direction
		//TODO - Improve me combine with above
		if(findCardinallyAdjacentProvinces(candidateCoord).size() == 0)
			return null;

		//Return province
		return adjacentProvinces.get(0);
	}
	
	private static List<Province> findAllAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		Province adjacentProvince;
		for(Coord adjacentCoord: findAllAdjacentCoords(givenCoord)) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null) {
				result.add(adjacentProvince);
			}
		}
		return new ArrayList<>(result);
	}

	public static Set<Coord> findAllAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		int[] x = new int[]{-1,0,1,-1,1,-1,0,1};
		int[] z = new int[]{-1,-1,-1,0,0,1,1,1};
		for(int i = 0; i < 8; i++) {
			result.add(new Coord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
		}
		return result;
	}

	private static List<Province> findCardinallyAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		Province adjacentProvince;
		for(Coord adjacentCoord: findCardinallyAdjacentCoords(givenCoord)) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null) {
				result.add(adjacentProvince);
			}
		}
		return new ArrayList<>(result);
	}
	
	private static Set<Coord> findCardinallyAdjacentCoords(Coord targetCoord) {
		Set<Coord> result = new HashSet<>();
		int[] x = new int[]{0,-1,1,0};
		int[] z = new int[]{-1,0,0,1};
		for(int i = 0; i < 4; i++) {
			result.add(new Coord(targetCoord.getX() + x[i], targetCoord.getZ() + z[i]));
		}
		return result;
	}
	
	private static boolean executeChunkClaimCompetition() {
		TownyProvinces.info("Chunk Claim Competition Started");
		
		//Create claim-brush objects
		List<ProvinceClaimBrush> provinceClaimBrushes = new ArrayList<>();
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, TownyProvincesSettings.getProvinceCreatorBrushSquareRadiusInChunks()));
		}
		
		//First claim once around the homeblocks
		for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
			claimChunksCoveredByBrush(provinceClaimBrush);
		}
		
		//Execute province painting competition
		for(int i = 0; i < TownyProvincesSettings.getNumberOfProvincePaintingCycles(); i++) {
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//If inactive, do nothing
				if(!provinceClaimBrush.isActive())
					continue;
				//Generate random move delta
				int minMoveAmount = TownyProvincesSettings.getProvinceCreatorBrushMinMoveInChunks();
				int maxMoveAmount = TownyProvincesSettings.getProvinceCreatorBrushMaxMoveInChunks();
				int moveDeltaX = TownyProvincesMathUtil.generateRandomInteger(-maxMoveAmount, maxMoveAmount);
				int moveDeltaZ = TownyProvincesMathUtil.generateRandomInteger(-maxMoveAmount, maxMoveAmount);
				//Apply min move amount
				moveDeltaX = moveDeltaX > 0 ? Math.max(moveDeltaX,minMoveAmount) : Math.min(moveDeltaX,-minMoveAmount);
				moveDeltaZ = moveDeltaZ > 0 ? Math.max(moveDeltaZ,minMoveAmount) : Math.min(moveDeltaZ,-minMoveAmount);
				//Move brush if possible
				Coord destination = new Coord(provinceClaimBrush.getCurrentPosition().getX() + moveDeltaX, provinceClaimBrush.getCurrentPosition().getZ() + moveDeltaZ);
				moveBrushIfPossible(provinceClaimBrush, destination);
				//Claim chunks
				claimChunksCoveredByBrush(provinceClaimBrush);
				//Deactivate if too many chuns have been claimed
				if(hasBrushHitClaimLimit(provinceClaimBrush)) {
					provinceClaimBrush.setActive(false);
				}
			}
		}
		TownyProvinces.info("Chunk Claim Competition Complete. Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getCoordProvinceMap().size());
		return true;
	}

	private static boolean hasBrushHitClaimLimit(ProvinceClaimBrush provinceClaimBrush) {
		double chunkArea = Math.pow(TownyProvincesSettings.getProvinceBlockSideLength(), 2);
		double currentClaimArea = provinceClaimBrush.getProvince().getCoordsInProvince().size() * chunkArea; 
		double claimAreaLimit = TownyProvincesSettings.getProvinceCreatorBrushClaimLimitInSquareMetres();
		return currentClaimArea > claimAreaLimit;
	}

	//Don't move if any of the brush would overlap another province, or adjacent to it
	private static void moveBrushIfPossible(ProvinceClaimBrush brush, Coord destination) {
		
		int brushMinX = destination.getX() - brush.getSquareRadius() ;
		int brushMaxX = destination.getX() + brush.getSquareRadius() ;
		int brushMinZ = destination.getZ() - brush.getSquareRadius();
		int brushMaxZ = destination.getZ() + brush.getSquareRadius();

		int provinceWorldMinX = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMinZ = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		
		Coord coord;
		Province province;
		for(int x = brushMinX -3; x <= (brushMaxX +3); x++) {
			for(int z = brushMinZ -3; z <= (brushMaxZ +3); z++) {
				
				//Don't move near edge of map
				if(x < provinceWorldMinX)
					return;
				else if (x > provinceWorldMaxX)
					return;
				else if (z < provinceWorldMinZ)
					return;
				else if (z > provinceWorldMaxZ)
					return;
				
				//Don't move near different province
				coord = new Coord(x,z);
				province = TownyProvincesDataHolder.getInstance().getProvinceAt(coord);
				if(province != null && province != brush.getProvince()) {
					return;
				}
			}
		}
		
		brush.moveBrush(destination);
	}

	/**
	 * Claim chunks covered by brush
	 * @param brush the brush
	 */
	private static void claimChunksCoveredByBrush(ProvinceClaimBrush brush) {
		int startX = brush.getCurrentPosition().getX() - brush.getSquareRadius();
		int endX = brush.getCurrentPosition().getX() + brush.getSquareRadius();
		int startZ = brush.getCurrentPosition().getZ() - brush.getSquareRadius();
		int endZ = brush.getCurrentPosition().getZ() + brush.getSquareRadius();
		for(int x = startX; x <= endX; x++) {
			for(int z = startZ; z <= endZ; z++) {
				//Claim chunk
				claimChunk(new Coord(x,z), brush.getProvince());
			}
		}
	}

	/**
	 * Claim the given chunk, unless another province has already claimed it
	 * Or near edge of map, or near other province, 
	 * @param coord co-ord of chunk
	 * @param province the province doing the claiming
	 */
	private static void claimChunk(Coord coord, Province province) {
		//Don't claim if already claimed
		if (TownyProvincesDataHolder.getInstance().getProvinceAt(coord) != null)
			return;

		//Don't claim near other province
		Set<Coord> adjacentCoords = findAllAdjacentCoords(coord);
		Province adjacentProvince;
		for(Coord adjacentCoord: adjacentCoords) {
			adjacentProvince = TownyProvincesDataHolder.getInstance().getProvinceAt(adjacentCoord);
			if(adjacentProvince != null &&adjacentProvince != province) {
				return;
			}
		}
		
		//Don't claim at edge of map
		int minX = (TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxX  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		int minZ = (TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxZ  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		if(coord.getX() < minX)
			return;
		else if (coord.getX() > maxX)
			return;
		else if (coord.getZ() < minZ)
			return;
		else if (coord.getZ() > maxZ)
			return;
		
		//Claim coord
		TownyProvincesDataHolder.getInstance().claimCoordForProvince(coord, province);
	}

	/**
	 * Create province homeblocks.
	 * Also create the homeblock objects at this point
	 * 
	 * @return false if we failed to create sufficient provinces
	 */
	private static boolean createProvinceHomeBlocks() {
		TownyProvinces.info("Now generating province homeblocks");
		Coord provinceHomeBlock;
		int idealNumberOfProvinces = calculateIdealNumberOfProvinces();
		for (int provinceIndex = 0; provinceIndex < idealNumberOfProvinces; provinceIndex++) {
			provinceHomeBlock = generateProvinceHomeBlock();
			if(provinceHomeBlock != null) { 
				//Province homeblock generated. Now create province
				Province province = new Province(provinceHomeBlock);
				TownyProvincesDataHolder.getInstance().addProvince(province);
			} else {
				//Could not generate a province homeblock. Ran out of space on the map
				double allowedVariance = TownyProvincesSettings.getMaxAllowedVarianceBetweenIdealAndActualNumProvinces();
				double minimumAllowedNumProvinces = ((double) idealNumberOfProvinces) * (1 - allowedVariance);
				int actualNumProvinces = TownyProvincesDataHolder.getInstance().getNumProvinces();
				if (actualNumProvinces < minimumAllowedNumProvinces) {
					TownyProvinces.severe("ERROR: Could not create the minimum number of provinces. Required: " + minimumAllowedNumProvinces + ". Actual: " + actualNumProvinces);
					return false;
				} else {
					TownyProvinces.info("" + actualNumProvinces + " province homeblocks created.");
					return true;
				}
			}
		}
		TownyProvinces.info("" + TownyProvincesDataHolder.getInstance().getNumProvinces() + " province homeblocks created.");
		return true;
	}

	/**
	 * Generate a new province homeBlock
	 * Return null if you fail - usually due to map being full up with provinces
	 */
	private static Coord generateProvinceHomeBlock() {
		double tpChunkSideLength = TownyProvincesSettings.getProvinceBlockSideLength();
		for(int i = 0; i < 100; i++) {
			
			//Establish boundaries
			double xLowest = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() +1;
			double xHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() -1;
			double zLowest = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() +1;
			double zHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() -1;
			
			//Generate coords
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / tpChunkSideLength);
			int zCoord = (int)(z / tpChunkSideLength);
			Coord generatedHomeBlockCoord = new Coord(xCoord, zCoord);
			
			//Validate
			if(validateProvinceHomeBlock(generatedHomeBlockCoord)) {
				return generatedHomeBlockCoord;
			}
		}
		TownyProvinces.info("Could not generate province homeblock");
		return null;
	}
	
	private static boolean validateProvinceHomeBlock(Coord coord) {
		int minAllowedDistanceInMetres = TownyProvincesSettings.getMinAllowedDistanceBetweenProvinceHomeBlocks();
		int minAllowedDistanceInChunks = minAllowedDistanceInMetres / TownyProvincesSettings.getProvinceBlockSideLength();
		List<Province> provinceList = TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList();
		for(Province province: provinceList) {
			if(MathUtil.distance(coord, province.getHomeBlock()) < minAllowedDistanceInChunks) {
				return false;
			}
		}
		return true;
	}
	
	private static int calculateIdealNumberOfProvinces() {
		double worldAreaSquareMetres = calculateWorldAreaSquareMetres();
		double averageProvinceAreaSquareMetres = TownyProvincesSettings.getProvinceSizeEstimateForPopulatingInSquareMetres();
		int idealNumberOfProvinces = (int)(worldAreaSquareMetres / averageProvinceAreaSquareMetres);
		TownyProvinces.info("Ideal num provinces: " + idealNumberOfProvinces);
		return idealNumberOfProvinces;
	}
	
	private static double calculateWorldAreaSquareMetres() {
		Location topLeftCorner = TownyProvincesSettings.getTopLeftCornerLocation();
		Location bottomRightCorner = TownyProvincesSettings.getBottomRightWorldCornerLocation();
		double sideLengthX = Math.abs(topLeftCorner.getX() - bottomRightCorner.getX());
		double sideLengthZ = Math.abs(topLeftCorner.getZ() - bottomRightCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;
	}
}

 