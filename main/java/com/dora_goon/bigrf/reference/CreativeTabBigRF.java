package com.dora_goon.bigrf.reference;

import com.dora_goon.bigrf.init.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabBigRF {
	public static final CreativeTabs BigRF_TAB = new CreativeTabs(Reference.Mod_ID){
		@Override
		public Item getTabIconItem(){
			return ModItems.fuel;
		}
		
		public String getTranslatedTabLabel(){
			return "Big RF";
		}
	};
}
