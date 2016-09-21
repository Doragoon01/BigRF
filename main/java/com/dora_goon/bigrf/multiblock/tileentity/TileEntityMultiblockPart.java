package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.gui.ContainerMultiblockController;
import com.dora_goon.bigrf.gui.GuiBoilerController;
import com.dora_goon.bigrf.gui.GuiEnergyCellController;
import com.dora_goon.bigrf.gui.GuiReactorController;
import com.dora_goon.bigrf.gui.GuiTurbineController;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import net.minecraft.entity.player.InventoryPlayer;

public class TileEntityMultiblockPart extends TileEntityMultiblockPartBase{

	public TileEntityMultiblockPart() {
		super();
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockMultiblockPart.isFrame(metadata)) { return; }
		
		throw new MultiblockValidationException(String.format("%d, %d, %d - Only casing may be used as part of a multiblock's frame", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		// All parts are valid for sides, by default
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		// All parts are valid for the top, by default
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		// All parts are valid for the bottom, by default
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - This part may not be placed in the multiblock's interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockMasterBase master) {
		super.onMachineAssembled(master);
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
	}

	@Override
	public void onMachineActivated() {
		// Re-render controllers on client
		if(this.worldObj.isRemote) {
			if(getBlockType() == Reference.blockMultiblockPart) {
				int metadata = this.getBlockMetadata();
				if(BlockMultiblockPart.isController(metadata)) {
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	@Override
	public void onMachineDeactivated() {
		// Re-render controllers on client
		if(this.worldObj.isRemote) {
			if(getBlockType() == Reference.blockMultiblockPart) {
				int metadata = this.getBlockMetadata();
				if(BlockMultiblockPart.isController(metadata)) {
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	// IMultiblockGuiHandler
	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 **/
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);		
		if(BlockMultiblockPart.isController(metadata)) {
			return new ContainerMultiblockController(this, inventoryPlayer.player);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if (!this.isConnected()) {
			LogHelper.info("TeMbP - Controller not connected");
			return null;
		}

		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		// TODO implament all multiblock types' gui
		if (BlockMultiblockPart.isCellController(metadata)) {
			LogHelper.info("TeMbP - Opening Energy Cell GUI");
			return new GuiEnergyCellController(new ContainerMultiblockController(this, inventoryPlayer.player), this);
		} else if (BlockMultiblockPart.isBoilerController(metadata)) {
			return new GuiBoilerController(new ContainerMultiblockController(this, inventoryPlayer.player), this);
		} else if (BlockMultiblockPart.isTurbineController(metadata)) {
			return new GuiTurbineController(new ContainerMultiblockController(this, inventoryPlayer.player), this);
		} else if (BlockMultiblockPart.isReactorController(metadata)) {
			return new GuiReactorController(new ContainerMultiblockController(this, inventoryPlayer.player), this);
		} else {return null;}
	}
	
}
