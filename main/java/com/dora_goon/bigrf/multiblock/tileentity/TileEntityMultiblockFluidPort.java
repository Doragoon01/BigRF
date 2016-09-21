package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.interfaces.INeighborUpdatableEntity;
import com.dora_goon.bigrf.multiblock.interfaces.ITickableMultiblockPart;

import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityMultiblockFluidPort extends TileEntityMultiblockPart implements IFluidHandler, INeighborUpdatableEntity, ITickableMultiblockPart {
	boolean inlet;
	IFluidHandler pumpDestination;

	public TileEntityMultiblockFluidPort(){
		super();

		inlet = true;
		pumpDestination = null;
	}


	public boolean isInlet() { return inlet; }

	public void setInlet(boolean shouldBeInlet, boolean markDirty) {
		if(inlet == shouldBeInlet) { return; }

		inlet = shouldBeInlet;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

		if(!worldObj.isRemote) {
			if(!inlet) {
				checkForAdjacentTank();
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
	public void onMachineAssembled(MultiblockMasterBase multiblockMasterBase)
	{
		super.onMachineAssembled(multiblockMasterBase);
		checkForAdjacentTank();

		this.notifyNeighborsOfTileChange();

		// Re-render on the client
		if(worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineBroken()
	{
		super.onMachineBroken();
		pumpDestination = null;

		if(worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
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


	// IFluidHandler
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(!isConnected() || !inlet || from != getOutwardsDir()) { return 0; }
		return ((MultiblockMasterBase) getMaster()).fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir()) { return null; }		
		return ((MultiblockMasterBase) getMaster()).drain(from,  resource, doDrain);
	}
	
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir()) { return null; }		
		return ((MultiblockMasterBase) getMaster()).drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || !inlet || from != getOutwardsDir()) { return false; }

		FluidTank cc = ((MultiblockMasterBase) getMaster()).getFluidTank();
		return cc.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir()) { return false; }
		FluidTank cc = ((MultiblockMasterBase) getMaster()).getFluidTank();
		return cc.getFluid().getFluid() == fluid;
	}

	private static FluidTankInfo[] emptyTankArray = null;
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(!isConnected() || from != getOutwardsDir()) { return emptyTankArray; }

		FluidTank cc = ((MultiblockMasterBase) getMaster()).getFluidTank();
		return ((MultiblockMasterBase) getMaster()).getTankInfo(from);
	}
	

	// ITickableMultiblockPart
	@Override
	public void onMultiblockServerTick() {
		// Try to pump steam out, if an outlet

	}


	// INeighborUpdatableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		checkForAdjacentTank();
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		checkForAdjacentTank();
	}


	// Private Helpers
	private IFluidHandler getConnectedTank() {
		return pumpDestination;
	}

	protected void checkForAdjacentTank()
	{
		pumpDestination = null;
		if(worldObj.isRemote || isInlet()) {
			return;
		}

		ForgeDirection outDir = getOutwardsDir();
		if(outDir == ForgeDirection.UNKNOWN) {
			return;
		}

		TileEntity neighbor = worldObj.getTileEntity(xCoord + outDir.offsetX, yCoord + outDir.offsetY, zCoord + outDir.offsetZ);
		if(neighbor instanceof IFluidHandler) {
			pumpDestination = (IFluidHandler)neighbor;
		}
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
	public void onMachineActivated() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMachineDeactivated() {
		// TODO Auto-generated method stub

	}


}
