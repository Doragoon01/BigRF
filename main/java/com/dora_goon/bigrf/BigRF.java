package com.dora_goon.bigrf;

import com.dora_goon.bigrf.handler.CommonPacketHandler;
import com.dora_goon.bigrf.handler.ConfigurationHandler;
import com.dora_goon.bigrf.handler.GUIHandler;
import com.dora_goon.bigrf.init.ModBlocks;
import com.dora_goon.bigrf.init.ModItems;
import com.dora_goon.bigrf.init.ModTileEntities;
import com.dora_goon.bigrf.proxy.IProxy;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;


@Mod(modid = Reference.Mod_ID, name = Reference.Mod_NAME, version=Reference.VERSION)

public class BigRF
{
			
	@Mod.Instance("BigRF")
	public static BigRF instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static IProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		LogHelper.info("Starting Pre-init");
		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigurationHandler());
		MinecraftForge.EVENT_BUS.register(proxy);
		
		CommonPacketHandler.init();
		ModItems.init();
		ModBlocks.init(0,true);
		ModTileEntities.init();	
		LogHelper.info("Finished Pre-init");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)	{
		LogHelper.info("Starting Init");
		proxy.registerEventHandlers();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GUIHandler());
		LogHelper.info("Finished Init");
	}	
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}
	
}
