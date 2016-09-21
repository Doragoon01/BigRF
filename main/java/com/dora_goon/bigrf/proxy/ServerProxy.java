package com.dora_goon.bigrf.proxy;

import com.dora_goon.bigrf.utility.LogHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class ServerProxy extends CommonProxy {

	@Override
    public void registerEventHandlers(){
		super.registerEventHandlers();
    	LogHelper.info("Registering Server Event Handlers");
        FMLCommonHandler.instance().bus().register(new MultiblockServerTickHandler());
    }

}
