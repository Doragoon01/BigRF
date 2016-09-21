package com.dora_goon.bigrf.multiblock;

import java.util.HashSet;
import java.util.Set;

import com.dora_goon.bigrf.gui.ISlotlessUpdater;
import com.dora_goon.bigrf.handler.ConfigurationHandler;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.interfaces.IActivateable;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluidPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluxPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.utility.LogHelper;

import cofh.api.energy.IEnergyHandler;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;

public class MultiblockBase extends RectangularMultiblockMasterBase implements IEnergyHandler{

	private float energyStored;
	private static int maxEnergyStored = 0;
	protected FluidTank fluidContainer;
	
	private Set<TileEntityMultiblockFluxPort> attachedFluxPorts;
	private Set<TileEntityMultiblockFluidPort> attachedFluidPorts;
	
	
	public MultiblockBase(World world) {
		super(world);
		energyStored = 0f;
		
		attachedFluxPorts = new HashSet<TileEntityMultiblockFluxPort>();
		attachedFluidPorts = new HashSet<TileEntityMultiblockFluidPort>();
		
	}


	@Override
	protected void onBlockAdded(IMultiblockPart part) {
		if(part instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.add((TileEntityMultiblockFluxPort)part);
		}

		if(part instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)part;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				LogHelper.info("MB - Controller has been added, something is wrong");
			}
		}
		
		if(part instanceof TileEntityMultiblockFluidPort) {
			attachedFluidPorts.add((TileEntityMultiblockFluidPort) part);
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart part) {
		if(part instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.remove((TileEntityMultiblockFluxPort)part);
		}

		if(part instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)part;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				LogHelper.info("Controller has been removed");
			}
		}
		
		if(part instanceof TileEntityMultiblockFluidPort) {
			attachedFluidPorts.remove((TileEntityMultiblockFluidPort)part);
		}
	}
	
	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		this.readFromNBT(data);
	}

	@Override
	protected void onMachineAssembled() {
		//  It won't be

	}

	@Override
	protected void onMachineRestored() {
		//  It won't be

	}

	@Override
	protected void onMachinePaused() {
		// It won't be

	}

	@Override
	protected void onMachineDisassembled() {
		// It won't be

	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// should be infinity
		return 0;
	}

	@Override
	protected int getMaximumXSize() {
		return ConfigurationHandler.Max_XZ;
	}

	@Override
	protected int getMaximumZSize() {
		return ConfigurationHandler.Max_XZ;
	}

	@Override
	protected int getMaximumYSize() {
		return ConfigurationHandler.Max_Y;
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		// it will never be whole
		throw new MultiblockValidationException("Not enough controllers. All multiblocks require at least 1.");
	}

	@Override
	protected void onAssimilated(MultiblockMasterBase assimilator) {
		this.attachedFluxPorts.clear();
	}
	
	@Override
	protected void onAssimilate(MultiblockMasterBase assimilated) {
		if(!(assimilated instanceof MultiblockBase)) {
			BeefCoreLog.warning("[%s] Base master @ %s is attempting to assimilate a non-base master! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockBase otherMaster = (MultiblockBase)assimilated;
		
		//merges a bunch of variables
		if(otherMaster.getEnergyStored() > this.getEnergyStored()) { this.setEnergyStored(otherMaster.getEnergyStored()); }

	}

	@Override
	public void updateClient() {}
	
	// Update loop. Only called while the machine is assembled.
	// so it should never run.
	@Override
	public boolean updateServer() {return false;}
		
	@Override
	public void writeToNBT(NBTTagCompound data) {
		// bunch of variables
		data.setInteger("maxEnergyStored", this.maxEnergyStored);
		data.setFloat("storedEnergy", this.energyStored);
		//TODO add fluid
		// data.set... from net.mincraft.nbttagcompound.
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("maxEnergyStored")) {
			maxEnergyStored = data.getInteger("maxEnergyStored");
		}
		if(data.hasKey("storedEnergy")) {
			setEnergyStored(Math.max(getEnergyStored(), data.getFloat("storedEnergy")));
		}
		//TODO add fluid
		// for each of the above, there's an if statement to get the data from the NBT
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
		//onFuelStatusChanged();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return false;
	}

	// IEnergyHandler	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		int amtMoved = (int)Math.min(maxReceive, (this.maxEnergyStored - this.energyStored));
		if(!simulate) {
			this.addStoredEnergy(amtMoved);
		}
		return amtMoved;
	}
	
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		int amtRemoved = (int)Math.min(maxExtract, this.energyStored);
		if(!simulate) {
			this.reduceStoredEnergy(amtRemoved);
		}
		return amtRemoved;
	}
	private int getOutlets(){
	    int outlets = 0;
	    for(TileEntityMultiblockFluxPort fluxPort : attachedFluxPorts){
	    	if (!fluxPort.isInlet() && fluxPort.hasEnergyConnection()){outlets++;}
	    }
		return outlets;
	}
	
	public float getEnergyStored() {
		return energyStored;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)energyStored;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return maxEnergyStored;
	}
	

	public void setEnergyStored(float oldEnergy) {
		energyStored = oldEnergy;
		if(energyStored < 0.0 || Float.isNaN(energyStored)) {
			energyStored = 0.0f;
		}
		else if(energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}	

	/**
	 * Add some energy to the internal storage buffer.
	 * Will not increase the buffer above the maximum or reduce it below 0.
	 * @param newEnergy
	 */
	protected void addStoredEnergy(float newEnergy) {
		if(Float.isNaN(newEnergy)) { return; }

		energyStored += newEnergy;
		if(energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
		if(-0.00001f < energyStored && energyStored < 0.00001f) {
			// Clamp to zero
			energyStored = 0f;
		}
		//LogHelper.info("MbC:aSE - Added: " + newEnergy + " | To end at: " + energyStored);
	}

	/**
	 * Remove some energy from the internal storage buffer.
	 * Will not reduce the buffer below 0.
	 * @param energy Amount by which the buffer should be reduced.
	 */
	protected void reduceStoredEnergy(float energy) {
		//LogHelper.info("Inverting Energy to add a negative");
		this.addStoredEnergy(-1f * energy);
	}
	
	
}
	
	
	
	
	
	