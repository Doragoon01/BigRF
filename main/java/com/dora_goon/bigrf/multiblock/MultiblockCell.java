package com.dora_goon.bigrf.multiblock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.dora_goon.bigrf.gui.ISlotlessUpdater;
import com.dora_goon.bigrf.handler.CommonPacketHandler;
import com.dora_goon.bigrf.handler.ConfigurationHandler;
import com.dora_goon.bigrf.handler.message.MultiblockUpdateMessage;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.interfaces.IActivateable;
import com.dora_goon.bigrf.multiblock.interfaces.ITickableMultiblockPart;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluidPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluxPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockItemPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.utility.GameAreaHelper;
import com.dora_goon.bigrf.utility.LogHelper;
import com.dora_goon.bigrf.utility.StaticUtils;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockRegistry;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.MultiblockWorldRegistry;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class MultiblockCell extends RectangularMultiblockMasterBase implements IEnergyHandler, IFluidHandler, IActivateable, ISlotlessUpdater {
	//stored game stuff
	protected boolean active;
	private float energyStored;
	protected FluidTank tank;
	public int TANK_SIZE = 0;
		
	int CellVolume;
	
	private Set<TileEntityMultiblockPart> attachedControllers;
	private Set<TileEntityMultiblockFluxPort> attachedFluxPorts;
	private Set<TileEntityMultiblockFluidPort> attachedFluidPorts;
	
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	public static final int TANK_INPUT = 0;
	private static int maxEnergyStored;
	
	public MultiblockCell(World world){
		super(world);
		active = false;
		energyStored = 0f;
		CellVolume = 0;
		
		
		tank = new FluidTank(FluidRegistry.getFluid("redstone"), 0, 0);
		
		attachedControllers = new HashSet<TileEntityMultiblockPart>();
		attachedFluxPorts = new HashSet<TileEntityMultiblockFluxPort>();
		attachedFluidPorts = new HashSet<TileEntityMultiblockFluidPort>();
		
		updatePlayers = new HashSet<EntityPlayer>();
		ticksSinceLastUpdate = 0;
	}

	@Override
	public void beginUpdatingPlayer(EntityPlayer playerToUpdate) {
		updatePlayers.add(playerToUpdate);
		sendIndividualUpdate(playerToUpdate);
	}
	
	@Override
	public void stopUpdatingPlayer(EntityPlayer playerToRemove) {
		updatePlayers.remove(playerToRemove);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	protected void onBlockAdded(IMultiblockPart part) {
		if(part instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.add((TileEntityMultiblockFluxPort)part);
		}

		if(part instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)part;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.add(reactorPart);
				LogHelper.info("Cell Controller has been added");
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
				attachedControllers.remove(reactorPart);

				if (attachedControllers.size()<1){
					// TODO make sure this is okay
					MultiblockRegistry.addDeadMaster(this.worldObj, this);
				}
			}
		}

		if(part instanceof TileEntityMultiblockFluidPort) {
			attachedFluidPorts.remove((TileEntityMultiblockFluidPort)part);
		}
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		// Ensure that there is at least one controller attached.
				
		if(attachedControllers.size() < 1) {
			throw new MultiblockValidationException("Not enough controllers. Energy Cells require at least 1.");
		}
		
		super.isMachineWhole();
	}
	
	@Override
	public void updateClient() {}
	
	// Update loop. Only called while the machine is assembled.
	@Override
	public boolean updateServer() {
		//stuff with heat and power
		float oldEnergy = this.getEnergyStored();

		if (getActive()){
			// Distribute power from cell to duct : orange
			int energyAvailable = (int)getEnergyStored();
			int energyRemaining = energyAvailable;
			int outPorts = getOutlets();
			if(outPorts > 0 && energyRemaining > 0) {
				// First, try to distribute fairly
				int splitEnergy = energyRemaining / outPorts;
				for(TileEntityMultiblockFluxPort fluxPort : attachedFluxPorts) {
					if(energyRemaining <= 0) { break; }
					if(!fluxPort.hasEnergyConnection() || fluxPort.isInlet()) { continue; }

					energyRemaining -= splitEnergy - fluxPort.onProvidePower(splitEnergy);
					//LogHelper.info("1.Unable to move " + energyRemaining + " units");
				}

				// Next, just hose out whatever we can, if we have any left
				if(energyRemaining > 0) {
					for(TileEntityMultiblockFluxPort fluxPort : attachedFluxPorts) {
						if(energyRemaining <= 0) { break; }
						if(!fluxPort.hasEnergyConnection() || fluxPort.isInlet()) { continue; }

						energyRemaining = fluxPort.onProvidePower(energyRemaining);
						//LogHelper.info("2.Unable to move " + energyRemaining + " units");
					}
				}
			}

			if(energyAvailable != energyRemaining) {
				reduceStoredEnergy((energyAvailable - energyRemaining));
				//LogHelper.info("3. Reducing " + (energyAvailable - energyRemaining) + " units in cell");
			}
		}
		
		// Send updates periodically
		ticksSinceLastUpdate++;	
		if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
			ticksSinceLastUpdate = 0;
			sendTickUpdate();
		}

		// Update any connected tickables (only in other multiblocks)

		//return true if energy stored changed
		//LogHelper.info("Current: " + this.getEnergyStored() + " | Old: " + oldEnergy);
		return (oldEnergy != this.getEnergyStored());
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
	
	public void setActive(boolean act) {
		if(act == this.active) { return; }
		this.active = act;
		
		for(IMultiblockPart part : connectedParts) {
			if(this.active) { part.onMachineActivated(); }
			else { part.onMachineDeactivated(); }
		}
		
		if(worldObj.isRemote) {
			// Force controllers to re-render on client
			for(IMultiblockPart part : attachedControllers) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
		}
		else {
			this.markReferenceCoordForUpdate();
		}
	}
	
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		if(world.isAirBlock(x, y, z)) { return; } // Air is OK

		//TODO add silver and lead
		Block block = world.getBlock(x, y, z);
		if(block == Blocks.redstone_block) {
			return;
		}
		
		/*
		Material material = world.getBlock(x, y, z).getMaterial();
		if(material == net.minecraft.block.material.MaterialLiquid.water) {
			return;
		}
		
		Block block = world.getBlock(x, y, z);
		if(block == Blocks.iron_block || block == Blocks.gold_block || block == Blocks.diamond_block || block == Blocks.emerald_block) {
			return;
		}
		
		// Permit registered moderator blocks
		int metadata = world.getBlockMetadata(x, y, z);

		if(ReactorInterior.getBlockData(ItemHelper.oreProxy.getOreName(new ItemStack(block, 1, metadata))) != null) {
			return;
		}
		*/
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		// bunch of variables
		data.setBoolean("CellActive", this.active);
		data.setInteger("maxEnergyStored", this.maxEnergyStored);
		data.setFloat("storedEnergy", this.energyStored);
		//TODO add fluid
		// data.set... from net.mincraft.nbttagcompound.
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("CellActive")) {
			setActive(data.getBoolean("CellActive"));
		}
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
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow 3x3x3 cube
		return 26;
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

	// Network & Storage methods
	/*
	 * Serialize a reactor into a given Byte buffer
	 * @param buf The byte buffer to serialize into
	 */
	public void serialize(ByteBuf buf) {
		// stuff
		buf.writeBoolean(active);
		buf.writeFloat(energyStored);
		
		}
	
	/*
	 * Deserialize a reactor's data from a given Byte buffer
	 * @param buf The byte buffer containing reactor data
	 */
	public void deserialize(ByteBuf buf) {
		//stuff
		setActive(buf.readBoolean());
		setEnergyStored(buf.readFloat());		
	}

	protected IMessage getUpdatePacket() {
		//LogHelper.info("New Packet");
        return new MultiblockUpdateMessage(this);
	}
			
	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }
		LogHelper.info("Sending Individual Update Packet");
        CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
	}
	
	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }
		LogHelper.info("Sending Tick Update Packet");
		for(EntityPlayer player : updatePlayers) {
            CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
		}
	}
	
	@Override
	protected void onAssimilated(MultiblockMasterBase assimilator) {
		this.attachedFluxPorts.clear();
		this.attachedControllers.clear();
	}
	
	@Override
	protected void onAssimilate(MultiblockMasterBase assimilated) {
		if(!(assimilated instanceof MultiblockCell) || !(assimilated instanceof MultiblockBase)) {
			BeefCoreLog.warning("[%s] Reactor @ %s is attempting to assimilate a non-Reactor machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockCell otherCell = (MultiblockCell)assimilated;
		
		//merges a bunch of variables
		if(otherCell.getEnergyStored() > this.getEnergyStored()) { this.setEnergyStored(otherCell.getEnergyStored()); }

	}
	
	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		this.readFromNBT(data);
	}

	@Override
	protected void onMachineAssembled() {
		recalculateDerivedValues();
	}

	@Override
	protected void onMachineRestored() {
		recalculateDerivedValues();
	}

	@Override
	protected void onMachinePaused() {
		// do nothing
	}
	
	@Override
	protected void onMachineDisassembled() {
		this.active = false;
	}

	private void recalculateDerivedValues() {
		//do a lot of math
		//TODO fluid stuff
		TANK_SIZE = 3000*getInteriorBlockCount(Block.getBlockFromName("FluidRedstone"));
		tank.setCapacity(TANK_SIZE);
		
		//TODO move the recalculating of the maxenergystorage into it's own class or member
		maxEnergyStored = getInteriorBlockCount(Blocks.redstone_block) * 2000000;
		if(maxEnergyStored == 0){maxEnergyStored += 40000;}
		calculateMultiblockVolume();		
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

	// IEnergyHandler
	//receive energy is called by anything trying to put energy into the cell
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

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return false;
	}

	
	/**
	 *  returns the number of ports that can output energy
	 *   to allow a more evenly dispersed energy
	 **/
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


	public int getEnergyStoredPercentage() {
		return (int)(this.energyStored / (float)this.maxEnergyStored * 100f);
	}
	
	// Coolant subsystem
	
	protected int getMultiblockVolume() {
		return CellVolume;
	}
	
	protected void calculateMultiblockVolume() {
		CoordTriplet minInteriorCoord = getMinimumCoord();
		minInteriorCoord.x += 1;
		minInteriorCoord.y += 1;
		minInteriorCoord.z += 1;
		
		CoordTriplet maxInteriorCoord = getMaximumCoord();
		maxInteriorCoord.x -= 1;
		maxInteriorCoord.y -= 1;
		maxInteriorCoord.z -= 1;
		
		CellVolume = StaticUtils.ExtraMath.Volume(minInteriorCoord, maxInteriorCoord);
	}

	protected int getInteriorBlockCount(Block block){
		CoordTriplet minInteriorCoord = getMinimumCoord();
		minInteriorCoord.x += 1;
		minInteriorCoord.y += 1;
		minInteriorCoord.z += 1;
		
		CoordTriplet maxInteriorCoord = getMaximumCoord();
		maxInteriorCoord.x -= 1;
		maxInteriorCoord.y -= 1;
		maxInteriorCoord.z -= 1;
		
		int blockcountA = GameAreaHelper.scanArea(block, worldObj, minInteriorCoord, maxInteriorCoord);
		int blockcountB = GameAreaHelper.traceCrystal(Blocks.redstone_block, worldObj, minInteriorCoord, maxInteriorCoord);
		LogHelper.info("blockcount: " + blockcountA + " | " + blockcountB);
		return Math.min(blockcountA, blockcountB);
	}
	
	protected void markReferenceCoordForUpdate() {
		CoordTriplet rc = getReferenceCoord();
		if(worldObj != null && rc != null) {
			worldObj.markBlockForUpdate(rc.x, rc.y, rc.z);
		}
	}
	
	protected void markReferenceCoordDirty() {
		if(worldObj == null || worldObj.isRemote) { return; }

		CoordTriplet referenceCoord = getReferenceCoord();
		if(referenceCoord == null) { return; }

		TileEntity saveTe = worldObj.getTileEntity(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		worldObj.markTileEntityChunkModified(referenceCoord.x, referenceCoord.y, referenceCoord.z, saveTe);
	}

	@Override
	public boolean getActive() {
		return this.active;
	}

	
	//IFluidHandler

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || !resource.isFluidEqual(tank.getFluid())){return null;}
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {		   
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return tank.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;	   
	}

	public FluidTank getFluidTank() {
		return tank;
	}
	
	private static final FluidTankInfo[] emptyTankInfo = new FluidTankInfo[0];
	
	@Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
    	return new FluidTankInfo[] { tank.getInfo() };
    }


		
}
