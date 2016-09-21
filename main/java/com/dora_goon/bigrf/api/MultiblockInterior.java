package com.dora_goon.bigrf.api;

import java.util.HashMap;
import java.util.Map;

public class MultiblockInterior {
	private static Map<String, MultiblockInteriorData> _multiblockModeratorBlocks = new HashMap<String, MultiblockInteriorData>();
	private static Map<String, MultiblockInteriorData> _multiblockModeratorFluids = new HashMap<String, MultiblockInteriorData>();


	

	public static MultiblockInteriorData getBlockData(String oreDictName) {
		return _multiblockModeratorBlocks.get(oreDictName);
	}

	public static MultiblockInteriorData getFluidData(String fluidName) {
		return _multiblockModeratorFluids.get(fluidName);
	}	
	
}
