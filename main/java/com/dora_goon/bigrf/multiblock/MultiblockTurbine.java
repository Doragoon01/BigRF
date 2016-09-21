package com.dora_goon.bigrf.multiblock;

import java.util.HashSet;
import java.util.Set;

import com.dora_goon.bigrf.api.CoilPartData;
import com.dora_goon.bigrf.api.TurbineCoil;
import com.dora_goon.bigrf.gui.ISlotlessUpdater;
import com.dora_goon.bigrf.handler.CommonPacketHandler;
import com.dora_goon.bigrf.handler.ConfigurationHandler;
import com.dora_goon.bigrf.handler.message.MultiblockUpdateMessage;
import com.dora_goon.bigrf.multiblock.block.BlockMultiblockPart;
import com.dora_goon.bigrf.multiblock.block.BlockTurbineRotorPart;
import com.dora_goon.bigrf.multiblock.interfaces.IActivateable;
import com.dora_goon.bigrf.multiblock.interfaces.ITickableMultiblockPart;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluxPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockRotorBearing;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockRotorPart;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.FloatUpdateTracker;
import com.dora_goon.bigrf.utility.LogHelper;
import com.dora_goon.bigrf.utility.StaticUtils;

import cofh.api.energy.IEnergyProvider;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockRegistry;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class MultiblockTurbine extends RectangularMultiblockMasterBase implements IEnergyProvider,IFluidHandler, IActivateable, ISlotlessUpdater{


	// UI updates
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	// Fluid tanks. Input = Steam, Output = Water.
	public static final int TANK_INPUT = 0;
	public static final int FLUID_NONE = -1;
	public static final int TANK_SIZE = 4000; // TODO make this deturmineable
	public static final int MAX_PERMITTED_FLOW = 2000;
		
	private FluidTank tank;
	
	static final float maxEnergyStored = 40000f;
	
	// Persistent game data
	float energyStored;
	boolean active;
	float rotorEnergy;
	boolean inductorEngaged;
	
	// Player settings
	int maxIntakeRate;

	// Derivable game data
	int bladeSurfaceArea; // # of blocks that are blades
	int rotorMass; // 10 = 1 standard block-weight
	int coilSize;  // number of blocks in the coils

	// Inductor dynamic constants - get from a table on assembly
	float inductorDragCoefficient = inductorBaseDragCoefficient;
	float inductionEfficiency = 0.5f; // Final energy rectification efficiency. Averaged based on coil material and shape. 0.25-0.5 = iron, 0.75-0.9 = diamond, 1 = perfect.
	float inductionEnergyExponentBonus = 1f; // Exponential bonus to energy generation. Use this for very rare materials or special constructs.

	// Rotor dynamic constants - calculate on assembly
	float rotorDragCoefficient = 0.01f; // RF/t lost to friction per unit of mass in the rotor.
	float bladeDrag			   = 0.00025f; // RF/t lost to friction, multiplied by rotor speed squared. 
	float frictionalDrag	   = 0f;

	// Penalize suboptimal shapes with worse drag (i.e. increased drag without increasing lift)
	// Suboptimal is defined as "not a christmas-tree shape". At worst, drag is increased 4x.

	// Game balance constants - some of these are modified by configs at startup
	public static int inputFluidPerBlade = 25; // mB
	public static float inductorBaseDragCoefficient = 0.1f; // RF/t extracted per coil block, multiplied by rotor speed squared.
	public static final float baseBladeDragCoefficient = 0.00025f; // RF/t base lost to aero drag per blade block. Includes a 50% reduction to factor in constant parts of the drag equation
	
	float energyGeneratedLastTick;
	int fluidConsumedLastTick;
	float rotorEfficiencyLastTick;
	
	
	private Set<IMultiblockPart> attachedControllers;
	private Set<ITickableMultiblockPart> attachedTickables;
	private Set<TileEntityMultiblockRotorBearing> attachedRotorBearings;
	private Set<TileEntityMultiblockFluxPort> attachedFluxPorts;
	private Set<TileEntityMultiblockRotorPart> attachedRotorShafts;
	private Set<TileEntityMultiblockRotorPart> attachedRotorBlades;
	

	// Data caches for validation
	private Set<CoordTriplet> foundCoils;

	private FloatUpdateTracker rpmUpdateTracker;
	
	private static final ForgeDirection[] RotorXBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.NORTH };
	private static final ForgeDirection[] RotorZBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.WEST };
	
	
	public MultiblockTurbine(World world) {
		super(world);

		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		
		tank = new FluidTank(FluidRegistry.getFluid("steam"), 0, TANK_SIZE);
		
		attachedControllers = new HashSet<IMultiblockPart>();
		attachedTickables = new HashSet<ITickableMultiblockPart>();
		attachedRotorBearings = new HashSet<TileEntityMultiblockRotorBearing>();
		attachedFluxPorts = new HashSet<TileEntityMultiblockFluxPort>();
		attachedRotorShafts = new HashSet<TileEntityMultiblockRotorPart>();
		attachedRotorBlades = new HashSet<TileEntityMultiblockRotorPart>();
		
		energyStored = 0f;
		active = false;
		inductorEngaged = true;
		rotorEnergy = 0f;
		maxIntakeRate = MAX_PERMITTED_FLOW;
		
		bladeSurfaceArea = 0;
		rotorMass = 0;
		coilSize = 0;
		energyGeneratedLastTick = 0f;
		fluidConsumedLastTick = 0;
		rotorEfficiencyLastTick = 1f;
		
		foundCoils = new HashSet<CoordTriplet>();
		
		rpmUpdateTracker = new FloatUpdateTracker(100, 5, 10f, 100f); // Minimum 10RPM difference for slow updates, if change > 100 RPM, update every 5 ticks
		
	}

	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }

        CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
	}

	protected IMessage getUpdatePacket() {
        return new MultiblockUpdateMessage(this);
	}

	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.updatePlayers.size() <= 0) { return; }

		for(EntityPlayer player : updatePlayers) {
            CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
		}
	}

	// MultiblockControllerBase overrides

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		this.readFromNBT(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		if(newPart instanceof TileEntityMultiblockRotorBearing) {
			this.attachedRotorBearings.add((TileEntityMultiblockRotorBearing)newPart);
		}
		
		if(newPart instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.add((TileEntityMultiblockFluxPort)newPart);
		}
		
		if(newPart instanceof ITickableMultiblockPart) {
			attachedTickables.add((ITickableMultiblockPart)newPart);
		}

		if(newPart instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)newPart;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.add(reactorPart);
				LogHelper.info("Turbine Controller has been added");
			}
		}		
		
		if(newPart instanceof TileEntityMultiblockRotorPart) {
			TileEntityMultiblockRotorPart turbinePart = (TileEntityMultiblockRotorPart)newPart;
			if(turbinePart.isRotorShaft()) {
				attachedRotorShafts.add(turbinePart);
			}
			
			if(turbinePart.isRotorBlade()) {
				attachedRotorBlades.add(turbinePart);
			}
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart oldPart) {
		if(oldPart instanceof TileEntityMultiblockRotorBearing) {
			this.attachedRotorBearings.remove(oldPart);
		}
		
		if(oldPart instanceof TileEntityMultiblockFluxPort) {
			attachedFluxPorts.remove((TileEntityMultiblockFluxPort)oldPart);
		}

		if(oldPart instanceof ITickableMultiblockPart) {
			attachedTickables.remove((ITickableMultiblockPart)oldPart);
		}
		

		if(oldPart instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart reactorPart = (TileEntityMultiblockPart)oldPart;
			if(BlockMultiblockPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.remove(reactorPart);

				if (attachedControllers.size()<1){
					// TODO make sure this is okay
					MultiblockRegistry.addDeadMaster(this.worldObj, this);
				}
			}
		}
		
		if(oldPart instanceof TileEntityMultiblockRotorPart) {
			TileEntityMultiblockRotorPart turbinePart = (TileEntityMultiblockRotorPart)oldPart;
			if(turbinePart.isRotorShaft()) {
				attachedRotorShafts.remove(turbinePart);
			}
			
			if(turbinePart.isRotorBlade()) {
				attachedRotorBlades.remove(turbinePart);
			}
		}
	}

	@Override
	protected void onMachineAssembled() {
		recalculateDerivedStatistics();
	}

	@Override
	protected void onMachineRestored() {
		recalculateDerivedStatistics();
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
		rotorMass = 0;
		bladeSurfaceArea = 0;
		coilSize = 0;
		
		rotorEnergy = 0f; // Kill energy when machines get broken by players/explosions
	}


	// Validation code
	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		if(attachedRotorBearings.size() != 1) {
			throw new MultiblockValidationException("Turbines require exactly 1 rotor bearing");
		}
		
		// Set up validation caches
		foundCoils.clear();
		
		super.isMachineWhole();
		
		// Now do additional validation based on the coils/blades/rotors that were found
		
		// Check that we have a rotor that goes all the way up the bearing
		TileEntityMultiblockPartBase rotorPart = attachedRotorBearings.iterator().next();
		LogHelper.info("MbT - RotorPart: " + rotorPart);
		
		//Get interior dimentions
		CoordTriplet minRotorCoord = getMinimumCoord();
		minRotorCoord.x += 1;
		minRotorCoord.y += 1;
		minRotorCoord.z += 1;
		CoordTriplet maxRotorCoord = getMaximumCoord();
		maxRotorCoord.x -= 1;
		maxRotorCoord.y -= 1;
		maxRotorCoord.z -= 1;
		
		// Rotor bearing must calculate outwards dir, as this is normally only calculated in onMachineAssembled().
		rotorPart.recalculateOutwardsDirection(minRotorCoord, maxRotorCoord);
		
		// Find out which way the rotor runs. Obv, this is inwards from the bearing.
		ForgeDirection rotorDir = rotorPart.getOutwardsDir().getOpposite();
		CoordTriplet rotorCoord = rotorPart.getWorldLocation();
		
		LogHelper.info("MbT - Rotor Bearing Position: " + rotorPart.getPartPosition());
		LogHelper.info("MbT - Rotor Dir: " + rotorDir);
		LogHelper.info("MbT - Rotor Shaft Max: " + maxRotorCoord + " |  Min: " + minRotorCoord);
		
		// Constrain min/max rotor coords to where the rotor bearing is and the block opposite it
		if(rotorDir.offsetX == 0) {
			LogHelper.info("direction X");
			minRotorCoord.x = maxRotorCoord.x = rotorCoord.x;
		}
		if(rotorDir.offsetY == 0) {
			LogHelper.info("direction Y");
			minRotorCoord.y = maxRotorCoord.y = rotorCoord.y;
		}
		if(rotorDir.offsetZ == 0) {
			LogHelper.info("direction Z");
			minRotorCoord.z = maxRotorCoord.z = rotorCoord.z;
		}

		// Figure out where the rotor ends and which directions are normal to the rotor's 4 faces (this is where blades emit from)

		CoordTriplet endRotorCoord = rotorCoord.equals(minRotorCoord) ? maxRotorCoord : minRotorCoord;
		//endRotorCoord.translate(rotorDir);

		ForgeDirection[] bladeDirections;
		if(rotorDir.offsetY != 0) { 
			bladeDirections = StaticUtils.CardinalDirections;
		}
		else if(rotorDir.offsetX != 0) {
			bladeDirections = RotorXBladeDirections;
		}
		else {
			bladeDirections = RotorZBladeDirections;
		}

		Set<CoordTriplet> rotorShafts = new HashSet<CoordTriplet>(attachedRotorShafts.size());
		Set<CoordTriplet> rotorBlades = new HashSet<CoordTriplet>(attachedRotorBlades.size());
		
		for(TileEntityMultiblockRotorPart part : attachedRotorShafts) {
			rotorShafts.add(part.getWorldLocation());
		}

		for(TileEntityMultiblockRotorPart part : attachedRotorBlades) {
			rotorBlades.add(part.getWorldLocation());
		}
		
		// Move along the length of the rotor, 1 block at a time
		boolean encounteredCoils = false;
		LogHelper.info("MbT - Checking Shaft from " + rotorCoord + " to " + endRotorCoord);
		while(!rotorShafts.isEmpty() && !rotorCoord.equals(endRotorCoord)) {
			rotorCoord.translate(rotorDir);
			// Ensure we find a rotor block along the length of the entire rotor
			if(!rotorShafts.remove(rotorCoord)) {
				throw new MultiblockValidationException(String.format("%s - This block must contain a rotor. The rotor must begin at the bearing and run the entire length of the turbine", rotorCoord));
			}
			LogHelper.info("MbT - Checking Shaft at " + rotorCoord + " | Shafts Remaining: " + rotorShafts.size());
			// Now move out in the 4 rotor normals, looking for blades and coils
			CoordTriplet checkCoord = rotorCoord.copy();
			boolean encounteredBlades = false;
			for(ForgeDirection bladeDir : bladeDirections) {
				checkCoord.copy(rotorCoord);
				boolean foundABlade = false;
				checkCoord.translate(bladeDir);
				
				// If we find 1 blade, we can keep moving along the normal to find more blades
				while(rotorBlades.remove(checkCoord)) {
					// We found a coil already?! NOT ALLOWED.
					if(encounteredCoils) {
						throw new MultiblockValidationException(String.format("%s - Rotor blades must be placed closer to the rotor bearing than all other parts inside a turbine", checkCoord));
					}
					foundABlade = encounteredBlades = true;
					checkCoord.translate(bladeDir);
				}

				// If this block wasn't a blade, check to see if it was a coil
				if(!foundABlade) {
					if(foundCoils.remove(checkCoord)) {
						encounteredCoils = true;

						// We cannot have blades and coils intermix. This prevents intermixing, depending on eval order.
						if(encounteredBlades) {
							throw new MultiblockValidationException(String.format("%s - Metal blocks must by placed further from the rotor bearing than all rotor blades", checkCoord));
						}
						
						// Check the two coil spots in the 'corners', which are permitted if they're connected to the main rotor coil somehow
						CoordTriplet coilCheck = checkCoord.copy();
						coilCheck.translate(bladeDir.getRotation(rotorDir));
						foundCoils.remove(coilCheck);
						coilCheck.copy(checkCoord);
						coilCheck.translate(bladeDir.getRotation(rotorDir.getOpposite()));
						foundCoils.remove(coilCheck);
					}
					// Else: It must have been air.
				}
			}
		}
		LogHelper.info("MbT - " + rotorCoord + " = " + endRotorCoord);
		if(!rotorCoord.equals(endRotorCoord)) {
			throw new MultiblockValidationException("The rotor shaft must extend the entire length of the turbine interior.");
		}
		
		// Ensure that we encountered all the rotor, blade and coil blocks. If not, there's loose stuff inside the turbine.
		if(!rotorShafts.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blocks that are not attached to the main rotor. All rotor blocks must be in a column extending the entire length of the turbine, starting from the bearing.", rotorShafts.size()));
		}

		if(!rotorBlades.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blades that are not attached to the rotor. All rotor blades must extend continuously from the rotor's shaft.", rotorBlades.size()));
		}
		
		if(!foundCoils.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d metal blocks which were not in a ring around the rotor. All metal blocks must be in rings, or partial rings, around the rotor.", foundCoils.size()));
		}

		// A-OK!
	}
	
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		// We only allow air and functional parts in turbines.

		// Air is ok
		if(world.isAirBlock(x, y, z)) { return; }

		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x,y,z);

		// Coil windings below here:
		if(getCoilPartData(x, y, z, block, metadata) != null) {
			foundCoils.add(new CoordTriplet(x,y,z));
			return;
		}

		// Everything else, gtfo
		throw new MultiblockValidationException(String.format("%d, %d, %d is invalid for a turbine interior. Only rotor parts, metal blocks and empty space are allowed.", x, y, z));
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow 5x5x4 cube (100 - 18), interior minimum is 3x3x2
		return 82;
	}

	@Override
	protected int getMaximumXSize() {
		return ConfigurationHandler.maximumTurbineSize;
	}

	@Override
	protected int getMaximumZSize() {
		return ConfigurationHandler.maximumTurbineSize;
	}

	@Override
	protected int getMaximumYSize() {
		return ConfigurationHandler.maximumTurbineHeight;
	}
	
	@Override
	protected int getMinimumXSize() { return 5; }

	@Override
	protected int getMinimumYSize() { return 4; }

	@Override
	protected int getMinimumZSize() { return 5; }
	
	@Override
	protected void onAssimilate(MultiblockMasterBase otherMachine) {
		if(!(otherMachine instanceof MultiblockTurbine)) {
			//BeefCoreLog.warning("[%s] Turbine @ %s is attempting to assimilate a non-Turbine machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockTurbine otherTurbine = (MultiblockTurbine)otherMachine;
		
		setRotorEnergy(Math.max(rotorEnergy, otherTurbine.rotorEnergy));
	}

	@Override
	protected void onAssimilated(MultiblockMasterBase assimilator) {
		attachedControllers.clear();
		attachedRotorBearings.clear();
		attachedTickables.clear();
		attachedFluxPorts.clear();
	}

	@Override
	protected boolean updateServer() {

		energyGeneratedLastTick = 0f;
		fluidConsumedLastTick = 0;
		rotorEfficiencyLastTick = 1f;
		
		// Generate energy based on steam
		int steamIn = 0; // mB. Based on water, actually. Probably higher for steam. Measure it.

		if(getActive()) {
			// Spin up via steam inputs, convert some steam back into water.
			// Use at most the user-configured max, or the amount in the tank, whichever is less.
			steamIn = Math.min(maxIntakeRate, tank.getFluidAmount());
			

			// Cap steam used to available space, if not venting
			int availableSpace = tank.getCapacity() - tank.getFluidAmount();
			steamIn = Math.min(steamIn, availableSpace);

		}
		
		if(steamIn > 0 || rotorEnergy > 0) {
			float rotorSpeed = getRotorSpeed();

			// RFs lost to aerodynamic drag.
			float aerodynamicDragTorque = (float)rotorSpeed * bladeDrag;

			float liftTorque = 0f;
			if(steamIn > 0) {
				// TODO: Lookup fluid parameters from a table
				float fluidEnergyDensity = 10f; // RF per mB

				// Cap amount of steam we can fully extract energy from based on blade size
				int steamToProcess = bladeSurfaceArea * inputFluidPerBlade;
				steamToProcess = Math.min(steamToProcess, steamIn);
				liftTorque = steamToProcess * fluidEnergyDensity;

				// Did we have excess steam for our blade size?
				if(steamToProcess < steamIn) {
					// Extract some percentage of the remaining steam's energy, based on how many blades are missing
					steamToProcess = steamIn - steamToProcess;
					float bladeEfficiency = 1f;
					int neededBlades = steamIn / inputFluidPerBlade; // round in the player's favor
					int missingBlades = neededBlades - bladeSurfaceArea;
					bladeEfficiency = 1f - (float)missingBlades / (float)neededBlades;
					liftTorque += steamToProcess * fluidEnergyDensity * bladeEfficiency;

					rotorEfficiencyLastTick = liftTorque / (steamIn * fluidEnergyDensity);
				}
			}

			// Yay for derivation. We're assuming delta-Time is always 1, as we're always calculating for 1 tick.
			// RFs available to coils
			float inductionTorque = inductorEngaged ? rotorSpeed * inductorDragCoefficient * coilSize : 0f;
			float energyToGenerate = (float)Math.pow(inductionTorque, inductionEnergyExponentBonus) * inductionEfficiency;
			if(energyToGenerate > 0f) {
				// Efficiency curve. Rotors are 50% less efficient when not near 900/1800 RPMs.
				float efficiency = (float)(0.25*Math.cos(rotorSpeed/(45.5*Math.PI))) + 0.75f;
				if(rotorSpeed < 500) {
					efficiency = Math.min(0.5f, efficiency);
				}

				generateEnergy(energyToGenerate * efficiency);
			}

			rotorEnergy += liftTorque + -1f*inductionTorque + -1f*aerodynamicDragTorque + -1f*frictionalDrag;
			if(rotorEnergy < 0f) { rotorEnergy = 0f; }
						
		}
		
		int energyAvailable = (int)getEnergyStored();
		int energyRemaining = energyAvailable;
		if(energyStored > 0 && attachedFluxPorts.size() > 0) {
			// First, try to distribute fairly
			int splitEnergy = energyRemaining / attachedFluxPorts.size();
			for(TileEntityMultiblockFluxPort powerTap : attachedFluxPorts) {
				if(energyRemaining <= 0) { break; }
				if(powerTap == null || !powerTap.isConnected()) { continue; }

				energyRemaining -= splitEnergy - powerTap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if(energyRemaining > 0) {
				for(TileEntityMultiblockFluxPort fluxPort : attachedFluxPorts) {
					if(energyRemaining <= 0) { break; }
					if(fluxPort == null || !fluxPort.isConnected()) { continue; }

					energyRemaining = fluxPort.onProvidePower(energyRemaining);
				}
			}
		}
		
		if(energyAvailable != energyRemaining) {
			reduceStoredEnergy((energyAvailable - energyRemaining));
		}
		
		for(ITickableMultiblockPart part : attachedTickables) {
			part.onMultiblockServerTick();
		}
		
		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
			sendTickUpdate();
			ticksSinceLastUpdate = 0;
		}
		
		if(rpmUpdateTracker.shouldUpdate(getRotorSpeed())) {
			markReferenceCoordDirty();
		}

		return energyGeneratedLastTick > 0 || fluidConsumedLastTick > 0;
	}

	@Override
	protected void updateClient() {
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("active", active);
		data.setFloat("rotorEnergy", rotorEnergy);
		data.setBoolean("inductorEngaged", inductorEngaged);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {		
		if(data.hasKey("active")) {
			setActive(data.getBoolean("active"));
		}
		
		if(data.hasKey("rotorEnergy")) {
			setRotorEnergy(data.getFloat("rotorEnergy"));
			
			if(!worldObj.isRemote) {
				rpmUpdateTracker.setValue(getRotorSpeed());
			}
		}
		
		if(data.hasKey("inductorEngaged")) {
			setInductorEngaged(data.getBoolean("inductorEngaged"), false);
		}
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
	}

	
	// Network Serialization
	/**
	 * Used when dispatching update packets from the server.
	 * @param buf ByteBuf into which the turbine's full status should be written
	 */
	public void serialize(ByteBuf buf) {
		// Capture compacted fluid data first
		int inputFluidID, inputFluidAmt;
		{
			FluidStack inputFluid;
			inputFluid = tank.getFluid();
						
			if(inputFluid == null || inputFluid.amount <= 0) {
				inputFluidID = FLUID_NONE;
				inputFluidAmt = 0;
			}
			else {
				inputFluidID = inputFluid.getFluid().getID();
				inputFluidAmt = inputFluid.amount;
			}
			
		}

		// User settings
		buf.writeBoolean(active);
		buf.writeBoolean(inductorEngaged);
		buf.writeInt(maxIntakeRate);

		// Basic stats
		buf.writeFloat(energyStored);
		buf.writeFloat(rotorEnergy);

		// Reportage statistics
		buf.writeFloat(energyGeneratedLastTick);
		buf.writeInt(fluidConsumedLastTick);
		buf.writeFloat(rotorEfficiencyLastTick);
		
		// Fluid data
		buf.writeInt(inputFluidID);
		buf.writeInt(inputFluidAmt);
	}
	
	/**
	 * Used when a status packet arrives on the client.
	 * @param buf ByteBuf containing serialized turbine data
	 */
	public void deserialize(ByteBuf buf) {
		// User settings
		setActive(buf.readBoolean());
		setInductorEngaged(buf.readBoolean(), false);
		setMaxIntakeRate(buf.readInt());
		
		// Basic data
		setEnergyStored(buf.readFloat());
		setRotorEnergy(buf.readFloat());
		
		// Reportage
		energyGeneratedLastTick = buf.readFloat();
		fluidConsumedLastTick = buf.readInt();
		rotorEfficiencyLastTick = buf.readFloat();
	
		// Fluid data
		int inputFluidID = buf.readInt();
		int inputFluidAmt = buf.readInt();
		int outputFluidID = buf.readInt();
		int outputFluidAmt = buf.readInt();

		if(inputFluidID == FLUID_NONE || inputFluidAmt <= 0) {
			tank.setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(inputFluidID);
			if(fluid == null) {
				BeefCoreLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting input tank to empty", inputFluidID);
				tank.setFluid(null);
			}
			else {
				tank.setFluid(new FluidStack(fluid, inputFluidAmt));
			}
		}

		if(outputFluidID == FLUID_NONE || outputFluidAmt <= 0) {
			tank.setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(outputFluidID);
			if(fluid == null) {
				BeefCoreLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting output tank to empty", outputFluidID);
				tank.setFluid(null);
			}
			else {
				tank.setFluid(new FluidStack(fluid, outputFluidAmt));
			}
		}
	}

	// IFluidHandler
	// TODO makes sure Fluid stuff works
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (resource == null || !resource.isFluidEqual(tank.getFluid())){return null;}
        return tank.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){return tank.drain(maxDrain, doDrain);}

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){return true;}

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){return true;}

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
    	return new FluidTankInfo[] { tank.getInfo() };
    }
    
    @Override
    public FluidTank getFluidTank(){
    	return tank;
    }
    
	// IEnergyProvider

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		int energyExtracted = Math.min((int)energyStored, maxExtract);
		
		if(!simulate) {
			energyStored -= energyExtracted;
		}
		
		return energyExtracted;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)energyStored;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return (int)maxEnergyStored;
	}

	private void setEnergyStored(float newEnergy) {
		if(Float.isInfinite(newEnergy) || Float.isNaN(newEnergy)) { return; }

		energyStored = Math.max(0f, Math.min(maxEnergyStored, newEnergy));
	}
	
	// Energy Helpers
	public float getEnergyStored() {
		return energyStored;
	}
	
	/**
	 * Remove some energy from the internal storage buffer.
	 * Will not reduce the buffer below 0.
	 * @param energy Amount by which the buffer should be reduced.
	 */
	protected void reduceStoredEnergy(float energy) {
		addStoredEnergy(-1f * energy);
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
	}

	public void setStoredEnergy(float oldEnergy) {
		energyStored = oldEnergy;
		if(energyStored < 0.0 || Float.isNaN(energyStored)) {
			energyStored = 0.0f;
		}
		else if(energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}
	
	/**
	 * Generate energy, internally. Will be multiplied by the BR Setting powerProductionMultiplier
	 * @param newEnergy Base, unmultiplied energy to generate
	 */
	protected void generateEnergy(float newEnergy) {
		newEnergy = newEnergy * ConfigurationHandler.powerProductionMultiplier * ConfigurationHandler.turbinePowerProductionMultiplier;
		energyGeneratedLastTick += newEnergy;
		addStoredEnergy(newEnergy);
	}
	

	// Activity state
	public boolean getActive() {
		return active;
	}

	public void setActive(boolean newValue) {
		if(newValue != active) {
			this.active = newValue;
			for(IMultiblockPart part : connectedParts) {
				if(this.active) { part.onMachineActivated(); }
				else { part.onMachineDeactivated(); }
			}
			
			CoordTriplet referenceCoord = getReferenceCoord();
			worldObj.markBlockForUpdate(referenceCoord.x, referenceCoord.y, referenceCoord.z);

			markReferenceCoordDirty();
		}
		
		if(worldObj.isRemote) {
			// Force controllers to re-render on client
			for(IMultiblockPart part : attachedControllers) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
			
			for(TileEntityMultiblockRotorPart part : attachedRotorBlades) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
			
			for(TileEntityMultiblockRotorPart part : attachedRotorShafts) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
		}
	}


	// Governor
	public int getMaxIntakeRate() { return maxIntakeRate; }

	public void setMaxIntakeRate(int newRate) {
		maxIntakeRate = Math.min(MAX_PERMITTED_FLOW, Math.max(0, newRate));
		markReferenceCoordDirty();
	}
	
	// for GUI use
	public int getMaxIntakeRateMax() { return MAX_PERMITTED_FLOW; }
	
	// ISlotlessUpdater
	//TODO figure out why this is needed
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
	
	private CoilPartData getCoilPartData(int x, int y, int z, Block block, int metadata) {
		
		//TODO change compleatly since i'm only allowing specific blocks
		
		return TurbineCoil.getBlockData(block.toString());
	}
	

	/**
	 * Recalculate rotor and coil parameters
	 */
	private void recalculateDerivedStatistics() {
		CoordTriplet minInterior, maxInterior;
		minInterior = getMinimumCoord();
		maxInterior = getMaximumCoord();
		minInterior.x++; minInterior.y++; minInterior.z++;
		maxInterior.x--; maxInterior.y--; maxInterior.z--;
		
		rotorMass = 0;
		bladeSurfaceArea = 0;
		coilSize = 0;
		float coilEfficiency = 0f;
		float coilBonus = 0f;
		float coilDragCoefficient = 0f;

		// Loop over interior space. Calculate mass and blade area of rotor and size of coils
		for(int x = minInterior.x; x <= maxInterior.x; x++) {
			for(int y = minInterior.y; y <= maxInterior.y; y++) {
				for(int z = minInterior.z; z <= maxInterior.z; z++) {
					Block block = worldObj.getBlock(x, y, z);
					int metadata = worldObj.getBlockMetadata(x, y, z);
					CoilPartData coilData = null;

					if(block == Reference.blockTurbineRotorPart) {
						rotorMass += Reference.blockTurbineRotorPart.getRotorMass(block, metadata);
						if(BlockTurbineRotorPart.isRotorBlade(metadata)) {
							bladeSurfaceArea += 1;
						} 
					} 
					
					coilData = getCoilPartData(x, y, z, block, metadata);
					if(coilData != null) {
						coilEfficiency += coilData.efficiency;
						coilBonus += coilData.bonus;
						coilDragCoefficient += coilData.energyExtractionRate;
						coilSize += 1;
					}
				} // end z
			} // end y
		} // end x loop - looping over interior
		
		// Precalculate some stuff now that we know how big the rotor and blades are
		frictionalDrag = rotorMass * rotorDragCoefficient * ConfigurationHandler.turbineMassDragMultiplier;
		bladeDrag = baseBladeDragCoefficient * bladeSurfaceArea * ConfigurationHandler.turbineAeroDragMultiplier;

		if(coilSize <= 0)
		{
			// Uh. No coil? Fine.
			inductionEfficiency = 0f;
			inductionEnergyExponentBonus = 1f;
			inductorDragCoefficient = 0f;
		}
		else
		{
			inductionEfficiency = (coilEfficiency * 0.33f) / coilSize;
			inductionEnergyExponentBonus = Math.max(1f, (coilBonus / coilSize));
			inductorDragCoefficient = (coilDragCoefficient / coilSize) * inductorBaseDragCoefficient;
		}
	}
	
	public float getRotorSpeed() {
		if(attachedRotorBlades.size() <= 0 || rotorMass <= 0) { return 0f; }
		return rotorEnergy / (attachedRotorBlades.size() * rotorMass);
	}

	public float getEnergyGeneratedLastTick() { return energyGeneratedLastTick; }
	public int   getFluidConsumedLastTick() { return fluidConsumedLastTick; }
	public int	 getNumRotorBlades() { return attachedRotorBlades.size(); }
	public float getRotorEfficiencyLastTick() { return rotorEfficiencyLastTick; }

	public float getMaxRotorSpeed() {
		return 2000f;
	}
	
	public int getRotorMass() {
		return rotorMass;
	}
	
	public boolean getInductorEngaged() {
		return inductorEngaged;
	}
	
	public void setInductorEngaged(boolean engaged, boolean markReferenceCoordDirty) {
		inductorEngaged = engaged;
		if(markReferenceCoordDirty)
			markReferenceCoordDirty();
	}
	

	private void setRotorEnergy(float newEnergy) {
		if(Float.isNaN(newEnergy) || Float.isInfinite(newEnergy)) { return; }
		rotorEnergy = Math.max(0f, newEnergy);
	}

	protected void markReferenceCoordDirty() {
		if(worldObj == null || worldObj.isRemote) { return; }

		CoordTriplet referenceCoord = getReferenceCoord();
		if(referenceCoord == null) { return; }

		rpmUpdateTracker.onExternalUpdate();
		
		TileEntity saveTe = worldObj.getTileEntity(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		worldObj.markTileEntityChunkModified(referenceCoord.x, referenceCoord.y, referenceCoord.z, saveTe);
		worldObj.markBlockForUpdate(referenceCoord.x, referenceCoord.y, referenceCoord.z);
	}
	

	// For client usage only
	public ForgeDirection getRotorDirection() {
		if(attachedRotorBearings.size() < 1) {
			return ForgeDirection.UNKNOWN;
		}
		
		if(!this.isAssembled()) {
			return ForgeDirection.UNKNOWN;
		}
		
		TileEntityMultiblockRotorBearing rotorBearing = attachedRotorBearings.iterator().next();
		return rotorBearing.getOutwardsDir().getOpposite();
	}
		

	@SideOnly(Side.CLIENT)
	public void resetCachedRotors() {
		for(TileEntityMultiblockRotorBearing bearing: attachedRotorBearings) {
			bearing.clearDisplayList();
		}
	}
	
	
}
