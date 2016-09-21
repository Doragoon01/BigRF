package com.dora_goon.bigrf.utility;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;

public class ModHelperBase {

	public static boolean useCofh;
	
	public void register() {}
	
	public static void detectMods() {

		useCofh = Loader.isModLoaded("CoFHCore");
		
	}

}
