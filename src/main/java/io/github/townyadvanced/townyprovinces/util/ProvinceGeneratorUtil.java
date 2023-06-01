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
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProvinceGeneratorUtil {
	
	/**
	 * Generate all provinces in the world
	 */
	public static boolean generateProvinces() {
		List<File> provinceGeneratorFiles = FileUtil.readProvinceGeneratorFiles();
		Collections.sort(provinceGeneratorFiles);
		
		//Paint all Provinces
		for (File provinceGeneratorFile : provinceGeneratorFiles) {
			if(!paintProvincesInRegion(provinceGeneratorFile)) {
				return false;
			}
		}
		
		//Cleanups and borders
		
		//Allocate unclaimed chunks to provinces.
		TownyProvincesSettings.setProvinceGenerationInstructions(provinceGeneratorFiles.get(0));
		if(!assignUnclaimedChunksToProvinces()) {
			return false;
		}
		
		//Create all border blocks
		if(!createProvinceBorderBlocks()) {
			return false;
		}

		//Cull ocean provinces
		Bukkit.getScheduler().runTaskAsynchronously(TownyProvinces.getPlugin(), new CullOceanProvincesTask() );
		
		return true;
	}
	
	private static boolean paintProvincesInRegion(File provinceGeneratorFile) {
		TownyProvinces.info("Now Painting Provinces In Region: " + provinceGeneratorFile.getName());

		//Setup settings with correct instructions
		TownyProvincesSettings.setProvinceGenerationInstructions(provinceGeneratorFile);

		//Delete existing provinces which are mostly in the given area
		if(!deleteExistingProvincesWhichAreMostlyInSpecifiedArea()) {
			return false;
		}
		
		//Create province objects - empty except for the homeblocks
		if(!createProvinceObjects()) {
			return false;
		}
		
		//Execute chunk claim competition
		if(!executeChunkClaimCompetition()) {
			return false;
		}

		TownyProvinces.info("Finished Painting Provinces In Region: " + provinceGeneratorFile.getName());
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
			List<ProvinceBlock> provinceBlocks = province.getProvinceBlocks();
			int numProvinceBlocksInSpecifiedArea = 0;
			for (ProvinceBlock provinceBlock : provinceBlocks) {
				if (provinceBlock.getCoord().getX() < minX)
					continue;
				else if (provinceBlock.getCoord().getX() > maxX)
					continue;
				else if (provinceBlock.getCoord().getZ() < minZ)
					continue;
				else if (provinceBlock.getCoord().getZ() > maxZ)
					continue;
				numProvinceBlocksInSpecifiedArea++;
			}
			if(numProvinceBlocksInSpecifiedArea > (provinceBlocks.size() / 2)) {
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
			int numProvincesProcessed = 0;
			int provincesDeleted = 0;
			List<Province> provinces = TownyProvincesDataHolder.getInstance().getCopyOfProvincesSetAsList();
			for(Province province: provinces) {
				if(!province.isDeleted() && isProvinceMainlyOcean(province)) {
					province.setDeleted(true);
					provincesDeleted++;
				}
				numProvincesProcessed ++;
				TownyProvinces.info("Now Deleting Ocean Provinces. Total/Processed/Deleted: " + provinces.size() + "/" + numProvincesProcessed + "/" + provincesDeleted);
			}
			TownyProvinces.info("Finished Deleting Ocean Provinces.");
		}
	}
	
	public static void debugShowBiome(int coordX, int coordZ) {
		int x = (coordX * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
		int z = (coordZ * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
		String worldName = TownyProvincesSettings.getWorldName();
		World world = Bukkit.getWorld(worldName);
		Biome biome = world.getHighestBlockAt(x,z).getBiome();
		TownyProvinces.info("Method 1 Biome at coord " + coordX + ", " + coordZ + " and location " + x + ", " + z + "is " + biome.name());
	}

	private static boolean isProvinceMainlyOcean(Province province) {
		List<ProvinceBlock> provinceBlocks = province.getProvinceBlocks();
		String worldName = TownyProvincesSettings.getWorldName();
		World world = Bukkit.getWorld(worldName);
		Biome biome;
		ProvinceBlock provinceBlock;
		ChunkSnapshot chunkSnapshot;
		Chunk chunk;
		Block block;
		for(int i = 0; i < 10; i++) {
			provinceBlock = provinceBlocks.get((int)(Math.random() * provinceBlocks.size()));
			int x = (provinceBlock.getCoord().getX() * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
			int z = (provinceBlock.getCoord().getZ() * TownyProvincesSettings.getProvinceBlockSideLength()) + 8;
			//location = new Location(world, x,y,z);
			//Load chunk to ensure correct biome is read
			
			
			//THIS WORKS!!!
			//biome = world.getHighestBlockAt(x,z).getBiome();
		//	waitUntilNoChunksAreLoaded(world);
		//	System.gc();
			//world.get
			//chunk = world.getChunkAt(x,z);
			//boolean wasLoaded = chunk.isLoaded();
			//chunk.load(false);
			//block = chunk.getBlock(8,64,8);
			//biome = block.getBiome();
			
			//if(!wasLoaded) {
				//world.loadChunk(x,z,false);
				//chunk.load(false);
			//}
			//world.loadChunk(x,z,false);
			//}
			//chunkSnapshot = world.getEmptyChunkSnapshot(x,z,true,false);
			//biome = world.getChunkAt(x,z).getChunkSnapshot(false,true,false).getBiome(8,64,8);
			//try (FileWriter ChunkSnapshot chunkSnapshot = world.getChunkAt(x,z).getChunkSnapshot(false,true,false)) {
			//	
			//}
			biome = world.getHighestBlockAt(x,z).getBiome();
			//biome = chunkSnapshot.getBiome(8,64, 8);
			//waitUntilNoChunksAreLoaded(world);



			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			/*
			if(!wasLoaded) {
				while(true) {
					if(chunk.isLoaded()) {
						TownyProvinces.info("Chunk stil loaded");
						chunk.unload(false);
						//world.unloadChunkRequest(x,z);
						//world.unloadChunk(x,z, false);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					} else {
						break;
					}
				}
			}
			*/
			 
			System.gc();
			
			 
			
			 


			
			//biome = location.getBlock().getBiome();

			//World world = Bukkit.getWorld(worldName);
			//location = new Location(world, x,y,z);
			//biome = chunkSnapshot.getBiome(8,64, 8);
			//Chunk chunk = world.getChunkAt(x,z);
			//biome = chunk.getBlock(8,64, 8).getBiome();
			//try {
//				Thread.sleep(600);
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
			//DynmapAPI a;
			//DynmapCommonAPI c;
			//DynmapTask dynmapTask;
			//dynmapTask.
			

			//TownyProvinces.info("BIOME NAME: " + biome.name());
			if(!biome.name().toLowerCase().contains("ocean") && !biome.name().toLowerCase().contains("beach")) {
				return false;
			}
		}
		return true;
	}
	
	

	private static void waitUntilNoChunksAreLoaded(World world) {
		Chunk[] loadedChunks;
		while(true) {
			loadedChunks = world.getLoadedChunks();
			TownyProvinces.info("Num Loaded Chunks: " + loadedChunks.length);
			if(loadedChunks.length <= 10) {
				return;
			} else {
					for(int i = 0; i < loadedChunks.length; i++) {
						if(loadedChunks[i].isForceLoaded()) {
							loadedChunks[i].setForceLoaded(false);
						}
						//for(Plugin plugin: loadedChunks[i].getPluginChunkTickets()) {
						//	TownyProvinces.info("Chunk was loaded by: " + plugin);
							//loadedChunks[i].addPluginChunkTicket(TownyProvinces.getPlugin());
						//}
						loadedChunks[i].unload(true);
					}
			}
		}
	}

	private static Set<Coord> findAllUnclaimedCoords() {
		Set<Coord> result = new HashSet<>();
		int minX = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int maxX  = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX() / TownyProvincesSettings.getProvinceBlockSideLength();
		int minZ = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ() / TownyProvincesSettings.getProvinceBlockSideLength();
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
		TownyProvinces.info("Now Creating border blocks");
		//This is fairly simple. Just turn everything remaining in the target area, into a border bloc
		ProvinceBlock provinceBlock;
		for(Coord unclaimedCoord: findAllUnclaimedCoords()) {
			provinceBlock = new ProvinceBlock(unclaimedCoord, null, true);
			TownyProvincesDataHolder.getInstance().addProvinceBlock(unclaimedCoord, provinceBlock);
		}
		TownyProvinces.info("Finished Creating border blocks");
		return true;
	}

	/**
	 * Assign unclaimed chunks to provinces where possible
	 * 
	 * @return true on method success
	 */
	private static boolean assignUnclaimedChunksToProvinces() {
		TownyProvinces.info("Now assigning unclaimed chunks to provinces. This could take a few minutes...");
		List<Coord> coordsEligibleForProvinceAssignment;
		Province province;
		ProvinceBlock newProvinceBlock;
		Set<Coord> unclaimedCoords = findAllUnclaimedCoords();
		while((coordsEligibleForProvinceAssignment = findCoordsEligibleForProvinceAssignment(unclaimedCoords)).size() > 0) {
			TownyProvinces.info("Num Unclaimed Chunks: " + unclaimedCoords.size());
			/*
			 * Cycle each eligible coord
			 * Assign it if it is still eligible when we get to it
			 */
			for(Coord coord: coordsEligibleForProvinceAssignment) {
				if((province = getProvinceIfCoordIsEligibleForProvinceAssignment(coord)) != null) {
					newProvinceBlock = new ProvinceBlock(coord, province, false);
					TownyProvincesDataHolder.getInstance().addProvinceBlock(coord, newProvinceBlock);
					unclaimedCoords.remove(newProvinceBlock.getCoord());
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
		TownyProvinces.info("Chunk Claim Competition Complete. Total Chunks Claimed: " + TownyProvincesDataHolder.getInstance().getProvinceBlocks().values().size());
		return true;
	}

	private static boolean hasBrushHitClaimLimit(ProvinceClaimBrush provinceClaimBrush) {
		double chunkArea = Math.pow(TownyProvincesSettings.getProvinceBlockSideLength(), 2);
		double currentClaimArea = provinceClaimBrush.getProvince().getProvinceBlocks().size() * chunkArea; 
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
		ProvinceBlock provinceBlock;
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
				provinceBlock = TownyProvincesDataHolder.getInstance().getProvinceBlock(coord);
				if(provinceBlock != null && provinceBlock.getProvince() != brush.getProvince()) {
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
				Province province = new Province(provinceHomeBlock, UUID.randomUUID());
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
			
			//Establish boundaries
			double xLowest = TownyProvincesSettings.getTopLeftCornerLocation().getBlockX();
			double xHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockX();
			double zLowest = TownyProvincesSettings.getTopLeftCornerLocation().getBlockZ();
			double zHighest = TownyProvincesSettings.getBottomRightWorldCornerLocation().getBlockZ();
			//Don't put homeblocks right at edge of map
			xLowest += ((xHighest - xLowest) * 0.01); 
			zLowest += ((zHighest - zLowest) * 0.01);
			xHighest -= ((xHighest - xLowest) * 0.01); 
			zHighest -= ((zHighest - zLowest) * 0.01);
			
			//Generate coords
			double x = xLowest + (Math.random() * (xHighest - xLowest));
			double z = zLowest + (Math.random() * (zHighest - zLowest));
			int xCoord = (int)(x / tpChunkSideLength);
			int zCoord = (int)(z / tpChunkSideLength);
			Coord generatedHomeBlockCoord = new Coord(xCoord, zCoord);
			
			//Validate
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
		Location topLeftCorner = TownyProvincesSettings.getTopLeftCornerLocation();
		Location bottomRightCorner = TownyProvincesSettings.getBottomRightWorldCornerLocation();
		double sideLengthX = Math.abs(topLeftCorner.getX() - bottomRightCorner.getX());
		double sideLengthZ = Math.abs(topLeftCorner.getZ() - bottomRightCorner.getZ());
		double worldAreaSquareMetres = sideLengthX * sideLengthZ;
		TownyProvinces.info("World Area square metres: " + worldAreaSquareMetres);
		return worldAreaSquareMetres;
	}
}

 