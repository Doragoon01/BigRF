package com.dora_goon.bigrf.gui;


public interface IBeefTooltipControl {
	boolean isMouseOver(int mouseX, int mouseY);
	String[] getTooltip();
	
	public boolean isVisible();
}
