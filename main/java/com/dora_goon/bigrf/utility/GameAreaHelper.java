package com.dora_goon.bigrf.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.dora_goon.bigrf.handler.ConfigurationHandler;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;

import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class GameAreaHelper {

	public static int scanArea(Block block, World world, CoordTriplet minCoord, CoordTriplet maxCoord) {
		int blockcount = 0;
		
		CoordTriplet atblock = minCoord.copy();
		
		while (atblock.y <= maxCoord.y) {
			atblock.x = minCoord.x;
			while (atblock.x <= maxCoord.x) {
				atblock.z = minCoord.z;
				while (atblock.z <= maxCoord.z) {
					Block test = world.getBlock(atblock.x, atblock.y, atblock.z);
					//LogHelper.info("Scanning at: " + atblock.toString());
					if (test == block) {
						blockcount++;
					}
					atblock.z++;
				}
				atblock.x++;
			}
			atblock.y++;
		}
		//LogHelper.info("ended Scanning at: " + atblock.toString());
		return blockcount;
	}

	public static int traceCrystal(Block block, World world, CoordTriplet coord1, CoordTriplet coord2) {

		List<CoordTriplet> connectedBlocks = new ArrayList<CoordTriplet>();
		Stack<CoordTriplet> traversingBlocks = new Stack<CoordTriplet>();

		CoordTriplet atblock = coord1.copy();
		boolean found = false;

		while (atblock.x <= coord2.x && !found) {
			while (atblock.z <= coord2.z && !found) {
				Block test = world.getBlock(atblock.x, atblock.y, atblock.z);
				//LogHelper.info("Looking at: " + atblock.toString());
				if (test == block) {
					found = true;
				} else {
					atblock.z++;
				}
			}
			if (!found) {
				atblock.z = coord1.z;
				atblock.x++;
			}
		}
		if (!found) {
			LogHelper.info("GHA - Block not found");
			return 0;
		}

		// start at coord

		if (world.getBlock(atblock.x, atblock.y, atblock.z) != block) {
			return 0;
		} else {
			traversingBlocks.add(atblock);
		}

		while (!traversingBlocks.isEmpty()) {
			atblock = traversingBlocks.pop();
			connectedBlocks.add(atblock);
			//LogHelper.info("GAH - Looking around: " + atblock.toString());
			int chk = 0;
			boolean i = false;
			boolean x = false;

			for (CoordTriplet nextblock : atblock.getNeighbors()) {
				Block ident = world.getBlock(nextblock.x, nextblock.y, nextblock.z);
				//LogHelper.info("GAH - Tracing at: " + nextblock.toString() + " | Found: " + ident.getLocalizedName());
				if (i == false) {
					if (ident == Blocks.air || ident instanceof BlockMultiblockPart) {
						x = false;
					} else {
						if (ident == block && !connectedBlocks.contains(nextblock) && !traversingBlocks.contains(nextblock)) {
							traversingBlocks.add(nextblock);
						}
						x = true;
					}
					i = !i;
				} else if (i == true && x == false) {
					if (ident == Blocks.air || ident instanceof BlockMultiblockPart) {
						x = false;
					} else {
						if (ident == block && !connectedBlocks.contains(nextblock) && !traversingBlocks.contains(nextblock)) {
							traversingBlocks.add(nextblock);
						}
						x = true;
					}
					i = !i;
				} else {
					if (ident == Blocks.air || ident instanceof BlockMultiblockPart) {
						x = false;
					} else {
						if (ident == block && !connectedBlocks.contains(nextblock) && !traversingBlocks.contains(nextblock)) {
							traversingBlocks.add(nextblock);
						}
						x = true;
					}
					chk++;
					i = !i;
				}
			}
			if (chk >= 3) {
				// TODO do something when a block has too many axis
			}
		}

		return connectedBlocks.size();
	}

}
