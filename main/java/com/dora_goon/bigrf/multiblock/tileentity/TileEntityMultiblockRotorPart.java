package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityMultiblockRotorPart extends TileEntityMultiblockPartBase {

	public TileEntityMultiblockRotorPart() {
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
	}

	public boolean isRotorShaft() {
		return BlockTurbineRotorPart.isRotorShaft(getBlockMetadata());
	}

	public boolean isRotorBlade() {
		return BlockTurbineRotorPart.isRotorBlade(getBlockMetadata());
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
