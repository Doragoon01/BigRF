package com.dora_goon.bigrf.init;

import com.dora_goon.bigrf.multiblock.tileentity.*;
import com.dora_goon.bigrf.utility.LogHelper;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModTileEntities {
	
	public static void init(){
		LogHelper.info("Registering TileEntities");
		
		GameRegistry.registerTileEntity(TileEntityMultiblockPart.class, "BRFMultiblockFrame");
		GameRegistry.registerTileEntity(TileEntityMultiblockFluxPort.class, "BRFMultiblockFluxPort");
		GameRegistry.registerTileEntity(TileEntityMultiblockItemPort.class, "BRFMultiblockItemPort");
		GameRegistry.registerTileEntity(TileEntityMultiblockFluidPort.class, "BRFMultiblockFluidPort");
		GameRegistry.registerTileEntity(TileEntityMultiblockRotorPart.class, "BRFTurbineRotorPart");
		GameRegistry.registerTileEntity(TileEntityMultiblockRotorBearing.class, "BRFTurbineRotorBearing");

		LogHelper.info("Done Registering Tile Entities");
	}
}
