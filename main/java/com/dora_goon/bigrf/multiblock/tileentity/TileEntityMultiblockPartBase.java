package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.multiblock.MultiblockBase;
import com.dora_goon.bigrf.multiblock.MultiblockBoiler;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.MultiblockReactor;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.interfaces.IActivateable;
import com.dora_goon.bigrf.multiblock.interfaces.IMultiblockGuiHandler;
import com.dora_goon.bigrf.utility.LogHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;
import net.minecraft.entity.player.InventoryPlayer;


public abstract class TileEntityMultiblockPartBase extends RectangularMultiblockTileEntityBase implements IMultiblockGuiHandler{

	public TileEntityMultiblockPartBase(){
		}
	
	public Object getMaster() {
		
		return this.getMultiblockMaster();
		}
	
	@Override
	public boolean canUpdate() { return false; }

	@Override
	public MultiblockMasterBase createNewMultiblock() {
		//TODO  deturmine what type of multiblock to create and create that one	
		
		if(!BlockMultiblockPart.isController(this.blockMetadata)){
			return new MultiblockBase(worldObj);
		}else{
			if(BlockMultiblockPart.isCellController(this.blockMetadata)){return new MultiblockCell(worldObj);}
			else if(BlockMultiblockPart.isBoilerController(this.blockMetadata)){return new MultiblockBoiler(worldObj);}
			else if(BlockMultiblockPart.isTurbineController(this.blockMetadata)){return new MultiblockTurbine(worldObj);}
			else if(BlockMultiblockPart.isReactorController(this.blockMetadata)){return new MultiblockReactor(worldObj);}
			else{
				LogHelper.info("TeMbPB - Unable to create new multiblock master");
				return null;
			}
		}
	}

	@Override
	public Class<? extends MultiblockMasterBase> getMultiblockMasterType() {
		//TODO return differant typs for differant masters
		if(!BlockMultiblockPart.isController(this.blockMetadata)){
			return MultiblockBase.class;
		}else{
			if(BlockMultiblockPart.isCellController(this.blockMetadata)){return MultiblockCell.class;}
			else if(BlockMultiblockPart.isBoilerController(this.blockMetadata)){return MultiblockBoiler.class;}
			else if(BlockMultiblockPart.isTurbineController(this.blockMetadata)){return MultiblockTurbine.class;}
			else if(BlockMultiblockPart.isReactorController(this.blockMetadata)){return MultiblockReactor.class;}
			else{
				LogHelper.info("TeMbPB - Unable to deturmine multiblock master type");
				return null;
			}
		}

	}

	@Override
	public void onMachineAssembled(MultiblockMasterBase master) {
		super.onMachineAssembled(master);
		
		// Re-render this block on the client
		if(worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		
		// Re-render this block on the client
		if(worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	// IMultiblockGuiHandler
	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 */
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return null;
	}
	
	// IActivateable
	/*
	@Override
	public CoordTriplet getReferenceCoord() {
		if(isConnected()) {
			return getMaster().getReferenceCoord();
		}
		else {
			return new CoordTriplet(xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public boolean getActive() {
		if(isConnected()) {
			return getMaster().getActive();
		}
		else {
			return false;
		}
	}
	
	@Override
	public void setActive(boolean active) {
		if(isConnected()) {
			getMaster().setActive(active);
		}
		else {
			BeefCoreLog.error("Received a setActive command at %d, %d, %d, but not connected to a multiblock master!", xCoord, yCoord, zCoord);
		}
	}
 */
}
	