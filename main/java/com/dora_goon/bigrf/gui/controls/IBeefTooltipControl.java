package com.dora_goon.bigrf.gui.controls;


public interface IBeefTooltipControl {
	boolean isMouseOver(int mouseX, int mouseY);
	String[] getTooltip();
	
	public boolean isVisible();
}
