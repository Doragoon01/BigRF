package com.dora_goon.bigrf.init;

import com.dora_goon.bigrf.item.ItemBigRF;
import com.dora_goon.bigrf.item.ItemFuel;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {
	public static final ItemBigRF fuel = new ItemFuel();
	
	public static void init(){
		GameRegistry.registerItem(fuel, "fuel");
	}
}
