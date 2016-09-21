package com.dora_goon.bigrf.gui;

import com.dora_goon.bigrf.gui.BeefIconManager;

public class BeefGuiIconManager extends BeefIconManager {


	public static final int OFF_OFF = 0;
	public static final int OFF_ON = 1;
	public static final int ON_OFF = 2;
	public static final int ON_ON = 3;
	//public static final int ENERGY_STORED = 4;
	//public static final int ENERGY_OUTPUT = 5;
	//public static final int UP_ARROW = 6;
	//public static final int DOWN_ARROW = 7;

	@Override
	protected String[] getIconNames() {
		return new String[] {
				"Off_off",
				"Off_on",
				"On_off",
				"On_on",
				//"energyStored",
				//"energyOutput",
				//"upArrow",
				//"downArrow"
		};
	}
	
	@Override
	protected String getPath() { return "controls/"; }
	
}
