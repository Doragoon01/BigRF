package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.BigRF;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;
import com.dora_goon.bigrf.utility.RotorInfo;
import com.dora_goon.bigrf.utility.StaticUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMultiblockRotorBearing extends
TileEntityMultiblockPartStandard {

	RotorInfo rotorInfo = null;
	Integer displayList = null;
	float angle = 0f;

	@SideOnly(Side.CLIENT)
	public Integer getDisplayList() { return displayList; }

	@SideOnly(Side.CLIENT)
	public void setDisplayList(int newList) { displayList = newList; }

	@SideOnly(Side.CLIENT)
	public void clearDisplayList() { displayList = null; }

	@SideOnly(Side.CLIENT)
	public float getAngle() { return angle; }

	@SideOnly(Side.CLIENT)
	public void setAngle(float newAngle) { angle = newAngle; }

	protected AxisAlignedBB boundingBox;

	@Override
	public void onMachineAssembled(MultiblockMasterBase master) {
		super.onMachineAssembled(master);
		displayList = null;
		// TODO remove this: calculateRotorInfo();
	}

	@SideOnly(Side.CLIENT)
	public RotorInfo getRotorInfo() {
		return rotorInfo;
	}

	public AxisAlignedBB getAABB() { return boundingBox; }

	private void calculateRotorInfo() {
		// Calculate bounding box
		// TODO fix multiblock master stuff

		MultiblockTurbine turbine = (MultiblockTurbine) getMaster();
		CoordTriplet minCoord = turbine.getMinimumCoord();
		minCoord.x += 1;
		minCoord.y += 1;
		minCoord.z += 1;
		CoordTriplet maxCoord = turbine.getMaximumCoord();
		maxCoord.x -= 1;
		maxCoord.y -= 1;
		maxCoord.z -= 1;


		boundingBox = AxisAlignedBB.getBoundingBox(minCoord.x, minCoord.y, minCoord.z, maxCoord.x + 1, maxCoord.y + 1, maxCoord.z + 1);

		if(worldObj.isRemote) {
			// Calculate rotor info
			rotorInfo = new RotorInfo();
			rotorInfo.rotorDirection = getOutwardsDir().getOpposite();
			switch(rotorInfo.rotorDirection) {
			case DOWN:
			case UP:
				rotorInfo.rotorLength = maxCoord.y - minCoord.y - 1;
				break;
			case EAST:
			case WEST:
				rotorInfo.rotorLength = maxCoord.x - minCoord.x - 1;
				break;
			case NORTH:
			case SOUTH:
				rotorInfo.rotorLength = maxCoord.z - minCoord.z - 1;
				break;
			case UNKNOWN:	
				break;
			} 

			CoordTriplet currentCoord = getWorldLocation();
			CoordTriplet bladeCoord = new CoordTriplet(0,0,0);
			LogHelper.info("TeMbRB - Rotor Direction: " + rotorInfo.rotorDirection);
			
			ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[rotorInfo.rotorDirection.ordinal()];
			
			rotorInfo.bladeLengths = new int[rotorInfo.rotorLength][4];

			int rotorPosition = 0;
			currentCoord.translate(rotorInfo.rotorDirection);

			while(rotorPosition < rotorInfo.rotorLength) {
				// Current block is a rotor
				// Get list of normals
				int bladeLength;
				ForgeDirection bladeDir;
				for(int bladeIdx = 0; bladeIdx < dirsToCheck.length; bladeIdx++) {
					bladeDir = dirsToCheck[bladeIdx];
					bladeCoord.copy(currentCoord);
					bladeCoord.translate(bladeDir);
					bladeLength = 0;
					while(worldObj.getBlock(bladeCoord.x, bladeCoord.y, bladeCoord.z) == Reference.blockTurbineRotorPart && bladeLength < 32) {
						bladeLength++;
						bladeCoord.translate(bladeDir);
					}

					rotorInfo.bladeLengths[rotorPosition][bladeIdx] = bladeLength;
				}

				rotorPosition++;
				currentCoord.translate(rotorInfo.rotorDirection);
			}
		}
	}
}
