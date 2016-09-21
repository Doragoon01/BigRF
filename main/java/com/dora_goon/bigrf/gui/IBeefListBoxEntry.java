package com.dora_goon.bigrf.gui;

import net.minecraft.client.gui.FontRenderer;

public interface IBeefListBoxEntry {

	public int getHeight();
	
	public void draw(FontRenderer fontRenderer, int x, int y, int backgroundColor, int foregroundColor);
	
}
