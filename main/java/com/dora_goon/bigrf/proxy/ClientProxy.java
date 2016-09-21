package com.dora_goon.bigrf.proxy;

import java.util.Set;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.dora_goon.bigrf.gui.BeefGuiIconManager;
import com.dora_goon.bigrf.gui.BeefIconManager;
import com.dora_goon.bigrf.gui.CommonBlockIconManager;
import com.dora_goon.bigrf.multiblock.block.BlockTurbineRotorPart;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockRotorBearing;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.renderer.RotorSimpleRenderer;
import com.dora_goon.bigrf.renderer.RotorSpecialRenderer;
import com.dora_goon.bigrf.utility.LogHelper;

import erogenousbeef.core.multiblock.MultiblockClientTickHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;


public class ClientProxy extends CommonProxy {
	public static BeefGuiIconManager GuiIcons;
	public static CommonBlockIconManager CommonBlockIcons;
	
	public static long lastRenderTime = Minecraft.getSystemTime();
	
	public ClientProxy() {
		GuiIcons = new BeefGuiIconManager();
		CommonBlockIcons = new CommonBlockIconManager();
	}
	
	@Override
    public void registerEventHandlers(){
		super.registerEventHandlers();
    	LogHelper.info("Registering Client Event Handlers");
		FMLCommonHandler.instance().bus().register(new MultiblockClientTickHandler());
        FMLCommonHandler.instance().bus().register(new MultiblockServerTickHandler());
        
		BlockTurbineRotorPart.renderId = RenderingRegistry.getNextAvailableRenderId();
		ISimpleBlockRenderingHandler rotorISBRH = new RotorSimpleRenderer();
		RenderingRegistry.registerBlockHandler(BlockTurbineRotorPart.renderId, rotorISBRH);	
		
		if(Reference.blockMultiblockPart != null) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMultiblockRotorBearing.class, new RotorSpecialRenderer());
		}
    }

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {
		if(event.map.getTextureType() == BeefIconManager.TERRAIN_TEXTURE) {
			GuiIcons.registerIcons(event.map);
			CommonBlockIcons.registerIcons(event.map);
		}
	}
       
}
