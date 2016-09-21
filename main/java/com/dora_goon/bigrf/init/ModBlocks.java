package com.dora_goon.bigrf.init;

import com.dora_goon.bigrf.block.BlockBigRF;
import com.dora_goon.bigrf.item.ItemBlockBigRF;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.block.BlockTurbineRotorPart;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraftforge.oredict.OreDictionary;

public class ModBlocks {
	
	public static void init(int ID, boolean require) {
		LogHelper.info("Registering Blocks");
		
		Reference.blockMultiblockPart = new BlockMultiblockPart(Material.iron);
		GameRegistry.registerBlock(Reference.blockMultiblockPart, ItemBlockBigRF.class, "BRFMultiblockPart");
		LogHelper.info("Registered: " + Reference.blockMultiblockPart);
		
		Reference.blockTurbineRotorPart = new BlockTurbineRotorPart(Material.iron);
		GameRegistry.registerBlock(Reference.blockTurbineRotorPart, ItemBlockBigRF.class, "BRFMultiblockRotorPart");
		LogHelper.info("Registered: " + Reference.blockTurbineRotorPart);
		
		
		}
}
