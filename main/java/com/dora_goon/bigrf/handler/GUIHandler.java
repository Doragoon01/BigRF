package com.dora_goon.bigrf.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import com.dora_goon.bigrf.gui.IBeefGuiEntity;
import com.dora_goon.bigrf.multiblock.interfaces.IMultiblockGuiHandler;

public class GUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		else if(te instanceof IMultiblockGuiHandler) {
			return ((IMultiblockGuiHandler)te).getContainer(player.inventory);
		}
		else if(te instanceof IBeefGuiEntity) {
			return ((IBeefGuiEntity)te).getContainer(player);
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		
		if(te instanceof IMultiblockGuiHandler) {
			IMultiblockGuiHandler part = (IMultiblockGuiHandler)te;
			return part.getGuiElement(player.inventory);
		}
		else if(te instanceof IBeefGuiEntity) {
			return ((IBeefGuiEntity)te).getGUI(player);
		}
		
		return null;
	}
	
}
