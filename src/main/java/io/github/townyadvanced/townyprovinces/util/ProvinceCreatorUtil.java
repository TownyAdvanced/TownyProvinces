package io.github.townyadvanced.townyprovinces.util;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.util.MathUtil;
import io.github.townyadvanced.townyprovinces.TownyProvinces;
import io.github.townyadvanced.townyprovinces.data.TownyProvincesDataHolder;
import io.github.townyadvanced.townyprovinces.objects.Province;
import io.github.townyadvanced.townyprovinces.objects.ProvinceBlock;
import io.github.townyadvanced.townyprovinces.objects.ProvinceClaimBrush;
import io.github.townyadvanced.townyprovinces.settings.TownyProvincesSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;

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
		
		//Execute chunk claim competition
		if(!executeChunkClaimCompetition()) {
			return false;
		}
		
		//Allocate unclaimed chunks to provinces. Do not create borders yet
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}

		if(!cullProvincesContainingJustOcean()) {
			return false;
		}
		
		//Create all border blocks
		if(!createProvinceBorderBlocks()) {
			return false;
		}
		
		TownyProvinces.info("Provinces Created: " + TownyProvincesDataHolder.getInstance().getNumProvinces());
		TownyProvinces.info("Province Blocks Created: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().size());
		return true;
	}

	/**
	 * Cull all provinces which have ONLY ocean biomes
	 * 
	 * @return true if there's no error
	 */
	private static boolean cullProvincesContainingJustOcean() {
		TownyProvinces.info("Now Deleting Provinces Containing Just Ocean.");
		for(Province province: TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList()) {
			if(!doesProvinceHaveAnyNonOceanBiomes(province)) {
				TownyProvincesDataHolder.getInstance().deleteProvince(province);
			}
		}
		TownyProvinces.info("Ocean provinces deleted.");
		return true;
	}

	private static boolean doesProvinceHaveAnyNonOceanBiomes(Province province) {
		String worldName = TownyProvincesSettings.getWorldName();
		Biome biome;
		int x;
		int y = 64;
		int z;
		for(ProvinceBlock provinceBlock: province.getProvinceBlocks()) {
			x = provinceBlock.getCoord().getX() * TownyProvincesSettings.getProvinceBlockSideLength();
			z = provinceBlock.getCoord().getZ() * TownyProvincesSettings.getProvinceBlockSideLength();
			biome = Bukkit.getWorld(worldName).getBiome(x, y, z);
			if(!biome.getKey().getKey().toLowerCase().contains("ocean")) {
				return true;
			}
		}
		return false;
	}
	
	private static Set<Coord> findAllUnclaimedCoords() {
		Set<Coord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Coord coord;
		ProvinceBlock provinceBlock;
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				coord = new Coord(x,z);
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock == null) {
					//This means the chunk is unclaimed
					result.add(coord);
				}
			}
		}
		return result;
	}
	
	private static boolean createProvinceBorderBlocks() {
		//This is fairly simple. Just turn everything remaining in the target area, into a border bloc
		ProvinceBlock provinceBlock;
		for(Coord unclaimedCoord: findAllUnclaimedCoords()) {
			provinceBlock = new ProvinceBlock(unclaimedCoord, null, true);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(unclaimedCoord, provinceBlock);
		}
		return true;
	}

	/**
	 * Assign unclaimed chunks to provinces where possible
	 * 
	 * @return true on method success
	 */
	private static boolean assignUnclaimedChunksToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces. This could take a few minutes...");
		//Todo - more efficient progress indicator pls
		List<Coord> coordsEligibleForProvinceAssignment;
		int indexOfCoordToAssign;
		Coord coordToAssign;	
		Province province;
		ProvinceBlock newProvinceBlock;
		while((coordsEligibleForProvinceAssignment = findCoordsEligibleForProvinceAssignment()).size() > 0) {
			TownyProvinces.info("Num Coords Eligible For Province Assignment: " + coordsEligibleForProvinceAssignment.size());
			//Pick a random coord to assign
			indexOfCoordToAssign = (int)(Math.random() * coordsEligibleForProvinceAssignment.size());
			coordToAssign = coordsEligibleForProvinceAssignment.get(indexOfCoordToAssign);
			//Assign the cord to the (assumed) single, nearby, cardinally adjacent, province
			province = findCardinallyAdjacentProvinces(coordToAssign).get(0);
			newProvinceBlock = new ProvinceBlock(coordToAssign, province, false);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(coordToAssign, newProvinceBlock);
		}
		TownyProvinces.info("Finished assigning unclaimed chunks to provinces.");
		return true;
	}
	
	private static List<Coord> findCoordsEligibleForProvinceAssignment() {
		int minX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		Set<Coord> resultSet = new HashSet<>();
		Set<Coord> allUnclaimedCoords = findAllUnclaimedCoords();
		
		for(Coord candidateCoord: allUnclaimedCoords) {
			
			//Filter out chunk if it is at edge of map
			if(candidateCoord.getX() <= minX)
				continue;
			else if (candidateCoord.getX() >= maxX)
				continue;
			else if (candidateCoord.getZ() <= minZ)
				continue;
			else if (candidateCoord.getZ() >= maxZ)
				continue;
			
			//Filter out chunk if it does not have exactly 1 adjacent province
			if(findAllAdjacentProvinces(candidateCoord).size() != 1)
				continue;
			
			//Filter out chunk if the adjacent province is NOT on a cardinal direction
			if(findCardinallyAdjacentProvinces(candidateCoord).size() == 0)
				continue;
			
			//Add candidate coord to result set
			resultSet.add(candidateCoord);
		}
		return new ArrayList<>(resultSet);
	}
	
	private static List<Province> findAllAdjacentProvinces(Coord givenCoord) {
		Set<Province> result = new HashSet<>();
		ProvinceBlock adjacentProvinceBlock;
		for(Coord adjacentCoord: findAllAdjacentCoords(givenCoord)) {
			adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(adjacentProvinceBlock != null && adjacentProvinceBlock.getProvince() != null) {
				result.add(adjacentProvinceBlock.getProvince());
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
		ProvinceBlock adjacentProvinceBlock;
		for(Coord adjacentCoord: findCardinallyAdjacentCoords(givenCoord)) {
			adjacentProvinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(adjacentProvinceBlock != null && adjacentProvinceBlock.getProvince() != null) {
				result.add(adjacentProvinceBlock.getProvince());
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
			provinceClaimBrushes.add(new ProvinceClaimBrush(province, 8));
		}
		
		//First claim once around the homeblocks
		for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
			claimChunksCoveredByBrush(provinceClaimBrush);
		}
		
		//Now Do claim competition
		//Loop each brush x times //TODO parameterize num loops
		for(int i = 0; i < 200; i++) {
			for(ProvinceClaimBrush provinceClaimBrush: provinceClaimBrushes) {
				//Move brush
				//Todo - parameterize the move amount
				int moveAmountX = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				int moveAmountZ = TownyProvincesMathUtil.generateRandomInteger(-4,4);
				Coord destination = new Coord(provinceClaimBrush.getCurrentPosition().getX() + moveAmountX, provinceClaimBrush.getCurrentPosition().getZ() + moveAmountZ);
				moveBrushIfPossible(provinceClaimBrush, destination);

				//Claim chunks
				claimChunksCoveredByBrush(provinceClaimBrush);
			}
		}
		
		TownyProvinces.info("Claim Competition Complete. Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().values().size());
		return true;
	}

	//Don't move if any of the brush would overlap another province, or adjacent to it
	private static void moveBrushIfPossible(ProvinceClaimBrush brush, Coord destination) {
		
		int brushMinX = destination.getX() - brush.getSquareRadius() ;
		int brushMaxX = destination.getX() + brush.getSquareRadius() ;
		int brushMinZ = destination.getZ() - brush.getSquareRadius();
		int brushMaxZ = destination.getZ() + brush.getSquareRadius();

		int provinceWorldMinX = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMinZ = TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		int provinceWorldMaxZ  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
		
		Coord coord;
		ProvinceBlock provinceBlock;
		for(int x = brushMinX -1; x <= brushMaxX +1; x++) {
			for(int z = brushMinZ -1; z <= brushMaxZ +1; z++) {
				
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
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock != null && provinceBlock.getProvince() != brush.getProvince()) {
					return;
				}
			}
		}
		
		brush.moveBrush(destination);
	}

	/**
	 * Claim chunks covered by brush EXCEPT those near adjacent provinces or the map edge
	 * @param brush
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
		if (TownyProvincesDataHolder.getInstance().getProvinceBlock(coord) != null)
			return;

		//Don't claim near other province
		Set<Coord> adjacentCoords = findAllAdjacentCoords(coord);
		ProvinceBlock provinceBlock;
		for(Coord adjacentCoord: adjacentCoords) {
			provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(adjacentCoord);
			if(provinceBlock != null && provinceBlock.getProvince() != province) {
				return;
			}
		}
		
		//Don't claim at edge of map
		int minX = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxX  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		int minZ = (TownyProvincesSettings.getTopLeftWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) + 1;
		int maxZ  = (TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength()) - 1;
		if(coord.getX() < minX)
			return;
		else if (coord.getX() > maxX)
			return;
		else if (coord.getZ() < minZ)
			return;
		else if (coord.getZ() > maxZ)
			return;
		
		//Claim chunk
		ProvinceBlock newProvinceBlock = new ProvinceBlock(coord, province, false);
		TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, newProvinceBlock);
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
				Province province = new Province(provinceHomeBlock);
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

 