package com.dora_goon.bigrf.gui.controls;

import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.dora_goon.bigrf.gui.BeefGuiBase;
import com.dora_goon.bigrf.gui.IBeefTooltipControl;

public class BeefGuiFluidBar extends BeefGuiIconProgressBar implements
		IBeefTooltipControl {

	IFluidHandler _entity;
	int tankIdx;
	
	public BeefGuiFluidBar(BeefGuiBase container, int x, int y,
			IFluidHandler entity, int tankIdx) {
		super(container, x, y);
		
		this._entity = entity;
		this.tankIdx = tankIdx;
	}

	@Override
	protected IIcon getProgressBarIcon() {
		FluidTankInfo[] tanks = this._entity.getTankInfo(null);
		if(tanks != null && tankIdx < tanks.length) {
			if(tanks[tankIdx].fluid != null) {
				return tanks[tankIdx].fluid.getFluid().getIcon();
			}
		}
		return null;
	}
	
	@Override
	protected float getProgress() {
		FluidTankInfo[] tanks = this._entity.getTankInfo(null);
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				return (float)tankFluid.amount / (float)tanks[tankIdx].capacity;
			}
		}
		return 0.0f;
	}
	
	@Override
	public String[] getTooltip() {
		if(!visible) { return null; }

		FluidTankInfo[] tanks = this._entity.getTankInfo(null);
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				String fluidName = tankFluid.getFluid().getLocalizedName(tankFluid);
				if(tankFluid.getFluid().getID() == FluidRegistry.WATER.getID()) {
					fluidName = "Water";
				}
				else if(tankFluid.getFluid().getID() == FluidRegistry.LAVA.getID()) {
					fluidName = "Lava";
				}

				return new String[] { fluidName, String.format("%d / %d mB", tankFluid.amount, tanks[tankIdx].capacity) };
			}
			else {
				return new String[] { "Empty", String.format("0 / %d mB", tanks[tankIdx].capacity) };
			}
		}
		return null;
	}

	@Override
	protected ResourceLocation getResourceLocation() {
		return net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;
	}
	
	@Override
	protected boolean drawGradationMarks() { return true; }
}
