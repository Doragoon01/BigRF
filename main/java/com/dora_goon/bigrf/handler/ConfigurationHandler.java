package com.dora_goon.bigrf.handler;

import java.io.File;

import com.dora_goon.bigrf.reference.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;


public class ConfigurationHandler {
	public static Configuration configuration;
	public static int Max_XZ = 7;
	public static int Max_Y = 7;
	public static int Min_XZ = 3;
	public static int Min_Y = 3;

	public static int maximumTurbineSize = 7;
	public static int maximumTurbineHeight = 7;
	
	public static float powerProductionMultiplier = 1.0f;
	public static float turbinePowerProductionMultiplier = 1.0f;
	public static float turbineAeroDragMultiplier = 1.0f;
	public static float turbineMassDragMultiplier = 1.0f;


	public static void init(File configFile){
		if (configuration == null){
			configuration = new Configuration(configFile);	
			loadConfiguration();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event){
		if (event.modID.equalsIgnoreCase(Reference.Mod_ID)){
			loadConfiguration();
		}
	}
	
	private static void loadConfiguration(){
		Max_XZ = configuration.getInt("Max_XZ", configuration.CATEGORY_GENERAL, 7, 3, 17, "Maximum horizontal length of multiblocks");
		Max_Y = configuration.getInt("Max_Y", configuration.CATEGORY_GENERAL, 7, 3, 17, "Maximum vertical length of multiblocks");
		Min_XZ = configuration.getInt("Min_XZ", configuration.CATEGORY_GENERAL, 3, 3, 17, "Minimum horizontal length of multiblocks");
		Min_Y = configuration.getInt("Min_Y", configuration.CATEGORY_GENERAL, 3, 3, 17, "Minimum vertical length of multiblocks");
	
		maximumTurbineSize = configuration.getInt("maximumTurbineSize", configuration.CATEGORY_GENERAL, 7, 3, 17, "Maximum horizontal length of turbines");
		maximumTurbineHeight = configuration.getInt("maximumTurbineHeight", configuration.CATEGORY_GENERAL, 7, 3, 17, "Maximum vertical length of turbines");
		
		powerProductionMultiplier = configuration.getFloat("powerProductionMultiplier", configuration.CATEGORY_GENERAL, 1, -10, 10, "Changes final power output");
		turbinePowerProductionMultiplier = configuration.getFloat("turbinePowerProductionMultiplier", configuration.CATEGORY_GENERAL, 1, -10, 10, "Changes turbine specific power output");
		turbineAeroDragMultiplier = configuration.getFloat("turbineAeroDragMultiplier", configuration.CATEGORY_GENERAL, 1, -10, 10, "Aerodynamic Drag");
		turbineMassDragMultiplier = configuration.getFloat("turbineMassDragMultiplier", configuration.CATEGORY_GENERAL, 1, -10, 10, "MassDrag");
		
		if (configuration.hasChanged()){
			configuration.save();
		}
	}
}

