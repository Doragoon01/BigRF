package com.dora_goon.bigrf.multiblock;

import java.util.HashSet;
import java.util.Set;

import com.dora_goon.bigrf.api.MultiblockInterior;
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
import com.dora_goon.bigrf.utility.StaticUtils;

import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
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
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;

public class MultiblockBoiler extends RectangularMultiblockMasterBase implements IActivateable, IFluidHandler {


	// Game stuff - stored
	protected boolean active;
	private float boilerHeat;
	private float fuelHeat;
	private float energyStored;
	protected FluidTank waterTank;
	private int tankCapacity;
	
	private int boilerVolume;
	
	// Lists of connected parts
	private Set<TileEntityMultiblockFluxPort> attachedFluxPorts;
	private Set<ITickableMultiblockPart> attachedTickables;
	private Set<TileEntityMultiblockItemPort> attachedItemPorts;
	private Set<TileEntityMultiblockPart> attachedControllers;
	private Set<TileEntityMultiblockFluidPort> attachedFluidPorts;
	
	// Updates
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	
	public MultiblockBoiler(World world) {
		super(world);

		// Game stuff
		active = false;
		boilerHeat = 0f;
		fuelHeat = 0f;
		energyStored = 0f;
		tankCapacity = 0;

		waterTank = new FluidTank(FluidRegistry.WATER, 0, tankCapacity);
		
		// Derived stats
		
		
		// UI and stats
		
		
		attachedFluxPorts = new HashSet<TileEntityMultiblockFluxPort>();
		attachedTickables = new HashSet<ITickableMultiblockPart>();
		attachedItemPorts = new HashSet<TileEntityMultiblockItemPort>();
		attachedControllers = new HashSet<TileEntityMultiblockPart>();
		attachedFluidPorts = new HashSet<TileEntityMultiblockFluidPort>();
		
		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		
		boilerVolume = 0;
	}

	

	public void beginUpdatingPlayer(EntityPlayer playerToUpdate) {
		updatePlayers.add(playerToUpdate);
		sendIndividualUpdate(playerToUpdate);
	}
	
	public void stopUpdatingPlayer(EntityPlayer playerToRemove) {
		updatePlayers.remove(playerToRemove);
	}
	
	@Override
	protected void onBlockAdded(IMultiblockPart part) {
		if(part instanceof TileEntityMultiblockItemPort) {
			attachedItemPorts.add((TileEntityMultiblockItemPort)part);
		}
		

		if(part instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.add((TileEntityMultiblockFluxPort)part);
		}

		if(part instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)part;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.add(reactorPart);
			}
		}

		if(part instanceof ITickableMultiblockPart) {
			attachedTickables.add((ITickableMultiblockPart)part);
		}
		
		if(part instanceof TileEntityMultiblockFluidPort) {
			attachedFluidPorts.add((TileEntityMultiblockFluidPort) part);
		}
		
	}
	
	@Override
	protected void onBlockRemoved(IMultiblockPart part) {
		if(part instanceof TileEntityMultiblockItemPort) {
			attachedItemPorts.remove((TileEntityMultiblockItemPort)part);
		}


		if(part instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.remove((TileEntityMultiblockFluxPort)part);
		}

		if(part instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)part;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.remove(reactorPart);
			}
		}

		if(part instanceof ITickableMultiblockPart) {
			attachedTickables.remove((ITickableMultiblockPart)part);
		}
		
		
		if(part instanceof TileEntityMultiblockFluidPort) {
			attachedFluidPorts.remove((TileEntityMultiblockFluidPort)part);
		}
		
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		// Ensure that there is at least one controller and control rod attached.
		
		if(attachedControllers.size() < 1) {
			throw new MultiblockValidationException("Not enough controllers. Reactors require at least 1.");
		}		
		super.isMachineWhole();
	}
	
	@Override
	public void updateClient() {}
	
	// Update loop. Only called when the machine is assembled.
	@Override
	public boolean updateServer() {
		//TODO write the main loop
		return false;
	}
	
	@Override
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

	// Heat management methods
	protected void addReactorHeat(float newCasingHeat) {
		if(Float.isNaN(newCasingHeat)) {
			return;
		}

		boilerHeat += newCasingHeat;
		// Clamp to zero to prevent floating point issues
		if(-0.00001f < boilerHeat && boilerHeat < 0.00001f) { boilerHeat = 0.0f; }
	}
	
	public float getReactorHeat() {
		return boilerHeat;
	}
	
	public void setReactorHeat(float newHeat) {
		if(Float.isNaN(newHeat)) {
			boilerHeat = 0.0f;
		}
		else {
			boilerHeat = newHeat;
		}
	}

	protected void addFuelHeat(float additionalHeat) {
		if(Float.isNaN(additionalHeat)) { return; }
		
		fuelHeat += additionalHeat;
		if(-0.00001f < fuelHeat & fuelHeat < 0.00001f) { fuelHeat = 0f; }
	}
	
	public float getFuelHeat() { return fuelHeat; }
	
	public void setFuelHeat(float newFuelHeat) {
		if(Float.isNaN(newFuelHeat)) { fuelHeat = 0f; }
		else { fuelHeat = newFuelHeat; }
	}
	

	// Static validation helpers
	// Water, air, and metal blocks
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		if(world.isAirBlock(x, y, z)) { return; } // Air is OK

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

		if(MultiblockInterior.getBlockData(ItemHelper.oreProxy.getOreName(new ItemStack(block, 1, metadata))) != null) {
			return;
		}

		// Permit TE fluids
		if(block != null) {
			if(block instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock)block).getFluid();
				String fluidName = fluid.getName();
				if(MultiblockInterior.getFluidData(fluidName) != null) { return; }

				throw new MultiblockValidationException(String.format("%d, %d, %d - The fluid %s is not valid for the reactor's interior", x, y, z, fluidName));
			}
			else {
				throw new MultiblockValidationException(String.format("%d, %d, %d - %s is not valid for the reactor's interior", x, y, z, block.getLocalizedName()));
			}
		}
		else {
			throw new MultiblockValidationException(String.format("%d, %d, %d - Null block found, not valid for the reactor's interior", x, y, z));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("reactorActive", this.active);
		data.setFloat("heat", this.boilerHeat);
		data.setFloat("fuelHeat", fuelHeat);
		data.setFloat("storedEnergy", this.energyStored);
		data.setTag("fluidTank", waterTank.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("reactorActive")) {
			setActive(data.getBoolean("reactorActive"));
		}
		
		if(data.hasKey("heat")) {
			setReactorHeat(Math.max(getReactorHeat(), data.getFloat("heat")));
		}
				
		if(data.hasKey("fuelHeat")) {
			setFuelHeat(data.getFloat("fuelHeat"));
		}
		
		if(data.hasKey("fuelContainer")) {
			waterTank.readFromNBT(data.getCompoundTag("fuelContainer"));
		}
				
		if(data.hasKey("coolantContainer")) {
			waterTank.readFromNBT(data.getCompoundTag("fluidContainer"));
		}
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow cube.
		return 26;
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
	}

	// Network & Storage methods
	/*
	 * Serialize a reactor into a given Byte buffer
	 * @param buf The byte buffer to serialize into
	 */
	public void serialize(ByteBuf buf) {
		int fuelTypeID, wasteTypeID, coolantTypeID, vaporTypeID;

		

		// Basic data
		buf.writeBoolean(active);
		buf.writeFloat(boilerHeat);
		buf.writeFloat(fuelHeat);
		
		// Statistics
		
		// Coolant data
		
	}

	/*
	 * Deserialize a reactor's data from a given Byte buffer
	 * @param buf The byte buffer containing reactor data
	 */
	public void deserialize(ByteBuf buf) {
		// Basic data
		setActive(buf.readBoolean());
		setReactorHeat(buf.readFloat());
		setFuelHeat(buf.readFloat());
		
		// Statistics
		

		// Coolant data
		int coolantTypeID = buf.readInt();
		int coolantAmt = buf.readInt();
		int vaporTypeID = buf.readInt();
		int vaporAmt = buf.readInt();

		// Fuel & waste data
		
		
	}
	
	protected IMessage getUpdatePacket() {
        return new MultiblockUpdateMessage(this);
	}
	
	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }

        CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
	}
	
	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }

		for(EntityPlayer player : updatePlayers) {
            CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
		}
	}
	

	@Override
	protected void onAssimilated(MultiblockMasterBase otherMachine) {
		this.attachedFluxPorts.clear();
		this.attachedTickables.clear();
		this.attachedItemPorts.clear();
		this.attachedControllers.clear();
	}
	
	@Override
	protected void onAssimilate(MultiblockMasterBase otherMachine) {
		if(!(otherMachine instanceof MultiblockBoiler)) {
			BeefCoreLog.warning("[%s] Boiler @ %s is attempting to assimilate a non-Boiler machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockMasterBase otherMultiblock = (MultiblockMasterBase)otherMachine;

		//TODO merge multiblock data
		
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
	}

	@Override
	protected void onMachineDisassembled() {
		this.active = false;
	}

	private void recalculateDerivedValues() {
		// Recalculate size of fuel/waste tank via fuel rods
		CoordTriplet minCoord, maxCoord;
		minCoord = getMinimumCoord();
		maxCoord = getMaximumCoord();
		
		// Calculate derived stats
		
		// Calculate heat transfer based on fuel rod environment
		

		// Calculate heat transfer to coolant system based on reactor interior surface area.
		// This is pretty simple to start with - surface area of the rectangular prism defining the interior.
		int xSize = maxCoord.x - minCoord.x - 1;
		int ySize = maxCoord.y - minCoord.y - 1;
		int zSize = maxCoord.z - minCoord.z - 1;
		
		int surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);
		
		

		// Calculate passive heat loss.
		// Get external surface area
		xSize += 2;
		ySize += 2;
		zSize += 2;
		
		surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);
		
		
		if(!worldObj.isRemote) {
			// Force an update of the client's multiblock information
			markReferenceCoordForUpdate();
		}
		
		calculateMultiblockVolume();
		
		if(attachedFluidPorts.size() > 0) {
			int outerVolume = StaticUtils.ExtraMath.Volume(minCoord, maxCoord) - boilerVolume;
			waterTank.setCapacity(Math.max(0, Math.min(50000, outerVolume * 100)));
		}
		else {
			waterTank.setCapacity(0);
		}
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
	public boolean getActive() {
		return this.active;
	}
	

	protected int getMultiblockVolume() {
		return boilerVolume;
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
		
		boilerVolume = StaticUtils.ExtraMath.Volume(minInteriorCoord, maxInteriorCoord);
	}

		
	//IFluidHandler

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return waterTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || !resource.isFluidEqual(waterTank.getFluid())){return null;}
		return waterTank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {		   
		return waterTank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return waterTank.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;	   
	}

	@Override
	public FluidTank getFluidTank() {
		return waterTank;
	}
	
	private static final FluidTankInfo[] emptyTankInfo = new FluidTankInfo[0];
	
	@Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
    	return new FluidTankInfo[] { waterTank.getInfo() };
    }
	
	
	// Client-only
	
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

	
}
