package com.dora_goon.bigrf.gui;

import com.dora_goon.bigrf.gui.controls.BeefGuiFluidBar;
import com.dora_goon.bigrf.gui.controls.BeefGuiIcon;
import com.dora_goon.bigrf.gui.controls.BeefGuiLabel;
import com.dora_goon.bigrf.gui.controls.BeefGuiPowerBar;
import com.dora_goon.bigrf.gui.controls.BeefGuiRpmBar;
import com.dora_goon.bigrf.gui.controls.GuiIconButton;
import com.dora_goon.bigrf.handler.CommonPacketHandler;
import com.dora_goon.bigrf.handler.message.MultiblockCommandActivateMessage;
import com.dora_goon.bigrf.handler.message.TurbineChangeInductorMessage;
import com.dora_goon.bigrf.handler.message.TurbineChangeMaxIntakeMessage;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.proxy.ClientProxy;
import com.dora_goon.bigrf.reference.Reference;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiTurbineController extends BeefGuiBase {

	TileEntityMultiblockPart part;
	MultiblockTurbine turbine;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	
	private BeefGuiIcon speedIcon;
	private BeefGuiLabel speedString;
	
	private BeefGuiIcon energyGeneratedIcon;
	private BeefGuiLabel energyGeneratedString;
	
	private BeefGuiIcon rotorEfficiencyIcon;
	private BeefGuiLabel rotorEfficiencyString;

	private BeefGuiIcon powerIcon;
	private BeefGuiPowerBar powerBar;
	private BeefGuiIcon steamIcon;
	private BeefGuiFluidBar steamBar;
	
	private BeefGuiIcon rpmIcon;
	private BeefGuiRpmBar rpmBar;

	private BeefGuiIcon governorIcon;
	private BeefGuiLabel governorString;
	private GuiIconButton btnGovernorUp;
	private GuiIconButton btnGovernorDown;
	
	private GuiIconButton btnActivate;
	private GuiIconButton btnDeactivate;
	
	private GuiIconButton btnVentAll;
	private GuiIconButton btnVentOverflow;
	private GuiIconButton btnVentNone;
	
	private BeefGuiIcon inductorIcon;
	private GuiIconButton btnInductorOn;
	private GuiIconButton btnInductorOff;
	
	public GuiTurbineController(Container container, TileEntityMultiblockPart tileEntityMultiblockPart) {
		super(container);
		
		this.part = tileEntityMultiblockPart;
		turbine = (MultiblockTurbine) part.getMaster();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(Reference.GUI_DIRECTORY + "BasicBackground.png");
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		btnActivate = new GuiIconButton(0, guiLeft + 6, guiTop + 142, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"), new String[] { EnumChatFormatting.AQUA + "Activate Turbine", "", "Enables flow of intake fluid to rotor.", "Fluid flow will spin up the rotor." });
		btnDeactivate = new GuiIconButton(1, guiLeft + 24, guiTop + 142, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"), new String[] { EnumChatFormatting.AQUA + "Deactivate Turbine", "", "Disables flow of intake fluid to rotor.", "The rotor will spin down." });
		
		registerControl(btnActivate);
		registerControl(btnDeactivate);
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Turbine Control", leftX, topY);
		topY += titleString.getHeight() + 4;

		statusString = new BeefGuiLabel(this, "", leftX, topY);
		topY += statusString.getHeight() + 4;
		
		powerBar = new BeefGuiPowerBar(this, guiLeft + 145, guiTop + 42, this.turbine);
		rpmBar = new BeefGuiRpmBar(this, guiLeft + 85, guiTop + 42, turbine, "Rotor Speed", new String[] {"Rotors kept overspeed for too", "long may fail.", "", "Catastrophically."});
		steamBar = new BeefGuiFluidBar(this, guiLeft + 24, guiTop + 42, turbine, MultiblockTurbine.TANK_INPUT);
		
		/*
		speedIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { EnumChatFormatting.AQUA + "Rotor Speed", "", "Speed of the rotor in", "revolutions per minute.", "", "Rotors perform best at 900", "or 1800 RPM.", "", "Speeds over 2000PM are overspeed", "and may cause a turbine to", "fail catastrophically." });
		speedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += speedIcon.getHeight() + 4;

		energyGeneratedIcon = new BeefGuiIcon(this, leftX+1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("energyOutput"), new String[] { EnumChatFormatting.AQUA + "Energy Output", "", "Turbines generate energy via", "metal induction coils placed", "around a spinning rotor.", "More, or higher-quality, coils", "generate energy faster."});
		energyGeneratedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += energyGeneratedIcon.getHeight() + 4;
		
		rotorEfficiencyIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("rotorEfficiency"), new String[] { EnumChatFormatting.AQUA + "Rotor Efficiency", "", "Rotor blades can only fully", String.format("capture energy from %d mB of", MultiblockTurbine.inputFluidPerBlade), "fluid per blade.", "", "Efficiency drops if the flow", "of input fluid rises past", "capacity."});
		rotorEfficiencyString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += rotorEfficiencyIcon.getHeight() + 4;

		
		powerIcon = new BeefGuiIcon(this, guiLeft + 153, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("energyStored"), new String[] { EnumChatFormatting.AQUA + "Energy Storage" });

		
		steamIcon = new BeefGuiIcon(this, guiLeft + 113, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("hotFluidIn"), new String[] { EnumChatFormatting.AQUA + "Intake Fluid Tank" });

		rpmIcon = new BeefGuiIcon(this, guiLeft + 93, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { EnumChatFormatting.AQUA + "Rotor Speed" });
	
		governorIcon = new BeefGuiIcon(this, guiLeft + 102, guiTop + 107, 16, 16, ClientProxy.GuiIcons.getIcon("flowRate"), new String[] { EnumChatFormatting.AQUA + "Flow Rate Governor", "", "Controls the maximum rate at", "which hot fluids are drawn", "from the turbine's intake tank", "and passed over the turbines.", "", "Effectively, the max rate at which", "the turbine will process fluids."});
		governorString = new BeefGuiLabel(this, "", guiLeft + 122, guiTop + 112);
		btnGovernorUp   = new GuiIconButton(2, guiLeft + 120, guiTop + 125, 18, 18, ClientProxy.GuiIcons.getIcon("upArrow"),   new String[] { EnumChatFormatting.AQUA + "Increase Max Flow Rate", "", "Higher flow rates will", "increase rotor speed.", "", "SHIFT: +10 mB", "CTRL: +100mB", "CTRL+SHIFT: +1000mB"});
		btnGovernorDown = new GuiIconButton(3, guiLeft + 140, guiTop + 125, 18, 18, ClientProxy.GuiIcons.getIcon("downArrow"), new String[] { EnumChatFormatting.AQUA + "Decrease Max Flow Rate", "", "Lower flow rates will", "decrease rotor speed.",  "", "SHIFT: -10 mB", "CTRL: -100mB", "CTRL+SHIFT: -1000mB"});

		inductorIcon = new BeefGuiIcon(this, leftX, guiTop + 105, 16, 16, ClientProxy.GuiIcons.getIcon("coil"), new String[] { EnumChatFormatting.AQUA + "Induction Coils", "", "Metal coils inside the turbine", "extract energy from the rotor", "and convert it into RF.", "", "These controls engage/disengage", "the coils."});
		btnInductorOn = new GuiIconButton(7, guiLeft + 24, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"), new String[] { EnumChatFormatting.AQUA + "Engage Coils", "", "Engages the induction coils.", "Energy will be extracted from", "the rotor and converted to RF.", "", "Energy extraction exerts drag", "on the rotor, slowing it down." });
		btnInductorOff = new GuiIconButton(8, guiLeft + 44, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"), new String[] { EnumChatFormatting.AQUA + "Disengage Coils", "", "Disengages the induction coils.", "Energy will NOT be extracted from", "the rotor, allowing it to", "spin faster." });
		
		btnVentAll = new GuiIconButton(4, guiLeft + 4, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventAllOff"), new String[] { EnumChatFormatting.AQUA + "Vent: All Exhaust", "", "Dump all exhaust fluids.", "The exhaust fluid tank", "will not fill."});
		btnVentOverflow = new GuiIconButton(5, guiLeft + 24, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventOverflowOff"), new String[] { EnumChatFormatting.AQUA + "Vent: Overflow Only", "", "Dump excess exhaust fluids.", "Excess fluids will be lost", "if exhaust fluid tank is full."});
		btnVentNone = new GuiIconButton(6, guiLeft + 44, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventNoneOff"), new String[] { EnumChatFormatting.AQUA + "Vent: Closed", "", "Preserve all exhaust fluids.", "Turbine will slow or halt", "fluid intake if exhaust", "fluid tank is full."});
		
		registerControl(speedIcon);
		registerControl(speedString);
		registerControl(energyGeneratedIcon);
		registerControl(energyGeneratedString);
		registerControl(rotorEfficiencyIcon);
		registerControl(rotorEfficiencyString);
		registerControl(powerIcon);
		registerControl(steamIcon);
		registerControl(rpmIcon);
		registerControl(governorIcon);
		registerControl(governorString);
		registerControl(btnGovernorUp);
		registerControl(btnGovernorDown);
		registerControl(btnVentAll);
		registerControl(btnVentOverflow);
		registerControl(btnVentNone);
		registerControl(inductorIcon);
		registerControl(btnInductorOn);
		registerControl(btnInductorOff);
		*/
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(powerBar);
		registerControl(rpmBar);
		registerControl(steamBar);



		
		updateStrings();
		updateTooltips();
	}

	private void updateStrings() {
		
		if(turbine.getActive()) {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_GREEN + "Active");
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_RED + "Inactive");
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		/*
		speedString.setLabelText(String.format("%.1f RPM", turbine.getRotorSpeed()));
		energyGeneratedString.setLabelText(String.format("%.0f RF/t", turbine.getEnergyGeneratedLastTick()));
		governorString.setLabelText(String.format("%d mB/t", turbine.getMaxIntakeRate()));
		
		if(turbine.getActive()) {
			if(turbine.getRotorEfficiencyLastTick() < 1f) {
				rotorEfficiencyString.setLabelText(String.format("%.1f%%", turbine.getRotorEfficiencyLastTick() * 100f));
			}
			else {
				rotorEfficiencyString.setLabelText("100%");
			}

			int numBlades = turbine.getNumRotorBlades();
			int fluidLastTick = turbine.getFluidConsumedLastTick();
			int neededBlades = fluidLastTick / MultiblockTurbine.inputFluidPerBlade;
			
			rotorEfficiencyString.setLabelTooltip(String.format("%d / %d blades", numBlades, neededBlades));
		}
		else {
			rotorEfficiencyString.setLabelText("Unknown");
		}
				
		if(turbine.getInductorEngaged())
		{
			btnInductorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnInductorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else
		{
			btnInductorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnInductorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		*/
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateStrings();
	}
	
	protected void updateTooltips() {
		
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			boolean newSetting = button.id == 0;
			if(newSetting != turbine.getActive()) {
                CommonPacketHandler.INSTANCE.sendToServer(new MultiblockCommandActivateMessage(turbine, newSetting));
            }
		}
		
		if(button.id == 2 || button.id == 3) {
			int exponent = 0;

			if(isShiftKeyDown()) {
				exponent += 1;
			}
			if(isCtrlKeyDown()) {
				exponent += 2;
			}

			int newMax = (int) Math.round(Math.pow(10, exponent));

			if(button.id == 3) { newMax *= -1; }
			
			newMax = Math.max(0, Math.min(turbine.getMaxIntakeRateMax(), turbine.getMaxIntakeRate() + newMax));

			if(newMax != turbine.getMaxIntakeRate()) {
                CommonPacketHandler.INSTANCE.sendToServer(new TurbineChangeMaxIntakeMessage(turbine, newMax));
			}
		}
				
		if(button.id == 7 || button.id == 8)
		{
			boolean newStatus = button.id == 7;
			if(newStatus != turbine.getInductorEngaged())
			{
                CommonPacketHandler.INSTANCE.sendToServer(new TurbineChangeInductorMessage(turbine, newStatus));
			}
		}
	}
}
