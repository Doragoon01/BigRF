package com.dora_goon.bigrf.reference;

import com.dora_goon.bigrf.block.BlockBigRF;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.block.BlockTurbineRotorPart;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;

public class Reference {
	public static final String Mod_ID = "BigRF";
	public static final String Mod_NAME = "Big RF";
	public static final String VERSION = "1.7.10-0.1";
	public static final String CHANNEL 	=  Mod_ID.toLowerCase();
	public static final String DEPENDENCIES = "required-after:Forge@[10.13.2.1291,);after:ThermalExpansion;";
	
	public static final String CLIENT_PROXY_CLASS = "com.dora_goon.bigrf.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "com.dora_goon.bigrf.proxy.ServerProxy";
	
	public static final String TEXTURE_NAME_PREFIX = "bigrf:";
	public static final String RESOURCE_PATH = "/assets/bigrf/";
	public static final String GUI_DIRECTORY = TEXTURE_NAME_PREFIX + "textures/gui/";
	public static final String TEXTURE_DIRECTORY = RESOURCE_PATH + "textures/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	

	
	// TODO figure out where to place these
	
	public static BlockTurbineRotorPart blockTurbineRotorPart = null;
	public static BlockMultiblockPart blockMultiblockPart = null;
	public static Fluid fluidSteam = null;
	public static BlockTurbineRotorPart blockTurbineRotorBearing = null;



}