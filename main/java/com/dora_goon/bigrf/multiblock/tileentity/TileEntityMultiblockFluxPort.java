package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.multiblock.interfaces.INeighborUpdatableEntity;
import com.dora_goon.bigrf.utility.LogHelper;

import cofh.api.energy.IEnergyHandler;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMultiblockFluxPort extends TileEntityMultiblockPart implements  IEnergyHandler, INeighborUpdatableEntity  {
	boolean inlet;
	IEnergyHandler	rfNetwork;
	//public int MaxOutput;
	//public int MaxInput;
	
	public TileEntityMultiblockFluxPort() {
		super();
		
		//MaxOutput = 400;
		//MaxInput  = 400;	
		inlet = true;
		rfNetwork = null;
	}
	

	public boolean isInlet() { return inlet; }

	public void setInlet(boolean shouldBeInlet, boolean markDirty) {
		if(inlet == shouldBeInlet) { return; }

		inlet = shouldBeInlet;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		if(!worldObj.isRemote) {
			if(!inlet) {
				//checkForAdjacentTank();
			}

			if(markDirty) {
				markDirty();
			}
			else {
				notifyNeighborsOfTileChange();
			}
		}
		else {
			notifyNeighborsOfTileChange();
		}
		LogHelper.info("TeMbFP - FluxPort is set to " + inlet);
	}
	
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	// IMultiblockPart
	@Override
	public void onAttached(MultiblockMasterBase newMaster) {
		super.onAttached(newMaster);
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	
	// MultiblockTileEntityBase
	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);
		
		packetData.setBoolean("inlet", inlet);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		
		if(packetData.hasKey("inlet")) {
			setInlet(packetData.getBoolean("inlet"), false);
		}
	}
	
	@Override
	public void onMachineAssembled(MultiblockMasterBase multiblockMasterBase) {
		super.onMachineAssembled(multiblockMasterBase);

		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		// Force a connection to the power taps
		this.notifyNeighborsOfTileChange();
	}

	// TileEntity
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("inlet")) {
			inlet = tag.getBoolean("inlet");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("inlet", inlet);
	}

	
	// Custom PowerTap methods
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(IBlockAccess world, int x, int y, int z) {
		boolean wasConnected = (rfNetwork != null);
		ForgeDirection out = getOutwardsDir();
		if(out == ForgeDirection.UNKNOWN) {
			wasConnected = false;
			rfNetwork = null;
		}
		else {
			// See if our adjacent non-reactor coordinate has a TE
			rfNetwork = null;

			TileEntity te = world.getTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityMultiblockFluxPort)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyHandler) {
					IEnergyHandler handler = (IEnergyHandler)te;
					if(handler.canConnectEnergy(out.getOpposite())) {
						rfNetwork = handler;
					}
				}
			}
			
		}
		
		boolean isConnected = (rfNetwork != null);
		if(wasConnected != isConnected) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	/** This will be called by the Master when this port should be providing power.
	 * @return Power units remaining after consumption.
	 */
	public int onProvidePower(int units) {
		if(rfNetwork == null) {
			return units;
		}
		
		ForgeDirection approachDirection = getOutwardsDir().getOpposite();
		int energyConsumed = rfNetwork.receiveEnergy(approachDirection, (int)units, false);
		units -= energyConsumed;
		//LogHelper.info("TeMbFP - " + units + " units not provided to " + rfNetwork);
		return units;
	}
	/** This will be called by the Reactor Controller when this tap should be recieving power.
	 * @return Power units remaining after addition.
	 */
	public int onRecievePower(int units) {
		if(rfNetwork == null) {
			return 0;
		}
		
		ForgeDirection approachDirection = getOutwardsDir().getOpposite();
		int energyAssumed = rfNetwork.extractEnergy(approachDirection, (int)units, false);
		//LogHelper.info("TeMbFP - " + energyAssumed + " units");
		units -= energyAssumed;
		//LogHelper.info("TeMbFP - " + units + " units not recieved from " + rfNetwork);
		return units;
	}

	// IEnergyConnection
	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return from == getOutwardsDir();
	}
	
	// IEnergyHandler
	//TODO implament returns for all multiblock types
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if(!this.isConnected() || !inlet)
			return 0;

		if(from == getOutwardsDir()) {
			return ((IEnergyHandler) this.getMaster()).receiveEnergy(from, maxReceive, simulate);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if(!this.isConnected() || inlet)
			return 0;

		//TODO implament returns for all multiblock types
		if(from == getOutwardsDir()) {
			return ((IEnergyHandler) this.getMaster()).extractEnergy(from, maxExtract, simulate);

		}
		return 0;
	}
	
	//TODO implament returns for all multiblock types
	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(!this.isConnected())
			return 0;

		return ((IEnergyHandler) this.getMaster()).getEnergyStored(from);
	}

	//TODO implament returns for all multiblock types
	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(!this.isConnected())
			return 0;

		return ((IEnergyHandler) this.getMaster()).getMaxEnergyStored(from);
	}
	
	public boolean hasEnergyConnection() { return rfNetwork != null; }

}
