package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.gui.ContainerSlotless;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import net.minecraft.entity.player.InventoryPlayer;

public class TileEntityMultiblockPartStandard extends TileEntityMultiblockPartBase {

	public TileEntityMultiblockPartStandard() {
		super();
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		if(getBlockMetadata() != BlockMultiblockPart.METADATA_FRAME) {
			throw new MultiblockValidationException(String.format("%d, %d, %d - only turbine housing may be used as part of the turbine's frame", xCoord, yCoord, zCoord));
		}
	}

	@Override
	public void isGoodForSides() {
	}

	@Override
	public void isGoodForTop() {
	}

	@Override
	public void isGoodForBottom() {
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		//if(getBlockMetadata() != BlockMultiblockPart.METADATA_FRAME) {throw new MultiblockValidationException(String.format("%d, %d, %d - this part is not valid for the interior of a turbine", xCoord, yCoord, zCoord));}
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		if(getBlockMetadata() == BlockMultiblockPart.METADATA_ENERGYCELL_CONTROLLER ||getBlockMetadata() == BlockMultiblockPart.METADATA_TURBINE_CONTROLLER) {
			//TODO fix this
			return null; // (Object)(new ContainerSlotless(getMaster(), inventoryPlayer.player));
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}

		if(getBlockMetadata() == BlockMultiblockPart.METADATA_ENERGYCELL_CONTROLLER) {
			return null; //new GuiTurbineController((Container)getContainer(inventoryPlayer), this);
		}
		return null;
	}

	@Override
	public void onMachineActivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getBlockMetadata() == BlockMultiblockPart.METADATA_ENERGYCELL_CONTROLLER) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineDeactivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getBlockMetadata() == BlockMultiblockPart.METADATA_ENERGYCELL_CONTROLLER) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}	
}
