package com.dora_goon.bigrf.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.utility.LogHelper;

import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;

public class ContainerMultiblockController extends Container {

	RectangularMultiblockMasterBase master;
	TileEntityMultiblockPart part;
	
	public ContainerMultiblockController(TileEntityMultiblockPart mbpart, EntityPlayer player) {
		//TODO get working
		part = mbpart;
		master = (RectangularMultiblockMasterBase) part.getMaster();
		
		LogHelper.info("CMbC - beginUpdatingPlayer");
		if (master instanceof MultiblockCell){
			((MultiblockCell) master).beginUpdatingPlayer(player);
		} else if(master instanceof MultiblockCell){
			((MultiblockTurbine) master).beginUpdatingPlayer(player);
		}else{
			//TODO something
		}
			
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void putStackInSlot(int slot, ItemStack stack) {
		return;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		//TODO get rid of cast or make work for all types
		
		if(part != null && master != null){
			if(master instanceof MultiblockCell){((MultiblockCell) master).stopUpdatingPlayer(player);}
			else if(master instanceof MultiblockTurbine){((MultiblockTurbine) master).stopUpdatingPlayer(player);}
		}
			
	}
}