package com.dora_goon.bigrf.gui;

import com.dora_goon.bigrf.gui.controls.BeefGuiFluidBar;
import com.dora_goon.bigrf.gui.controls.BeefGuiLabel;
import com.dora_goon.bigrf.gui.controls.BeefGuiPowerBar;
import com.dora_goon.bigrf.gui.controls.GuiIconButton;
import com.dora_goon.bigrf.handler.CommonPacketHandler;
import com.dora_goon.bigrf.handler.message.MultiblockCommandActivateMessage;
import com.dora_goon.bigrf.multiblock.MultiblockBoiler;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.proxy.ClientProxy;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiBoilerController extends BeefGuiBase {

	private GuiIconButton btnMultiblockOn;
	private GuiIconButton btnMultiblockOff;

	private TileEntityMultiblockPart part;
	private MultiblockBoiler master;	

	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	private BeefGuiLabel VolumeString;

	private BeefGuiFluidBar waterBar;


	public GuiBoilerController(Container container, TileEntityMultiblockPart tileEntityMultiblockPart){
		super(container);
		
		ySize = 186;
		
		this.part = tileEntityMultiblockPart;
		this.master = (MultiblockBoiler) part.getMaster();
	}

	@Override
	public void initGui() {
		super.initGui();

		btnMultiblockOn = new GuiIconButton(0, guiLeft + 6, guiTop + 142, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"));
		btnMultiblockOff = new GuiIconButton(1, guiLeft + 24, guiTop + 142, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"));
		
		btnMultiblockOn.setTooltip(new String[] { EnumChatFormatting.AQUA + "Activate Multiblock" });
		btnMultiblockOff.setTooltip(new String[] { EnumChatFormatting.AQUA + "Deactivate Multiblock" });
		
		registerControl(btnMultiblockOn);
		registerControl(btnMultiblockOff);
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Boiler Control", leftX, topY);
		topY += titleString.getHeight() + 8;
	
		statusString = new BeefGuiLabel(this, "", leftX+1, topY);
		topY += statusString.getHeight() + 4;
		
		waterBar = new BeefGuiFluidBar(this, guiLeft + 24, guiTop + 42, master, MultiblockCell.TANK_INPUT);


		registerControl(titleString);
		registerControl(statusString);
		registerControl(waterBar);
		
		updateIcons();
	}

	protected void updateIcons() {
		if(master.getActive()) {
			btnMultiblockOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnMultiblockOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			btnMultiblockOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnMultiblockOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}			
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateIcons();
		
		if(master.getActive()) {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_GREEN + "Online");
		}
		else {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_RED + "Offline");
		}
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			boolean newSetting = button.id == 0;
			if(newSetting != master.getActive()) {
				LogHelper.info("Sending Message: " + master + " | " + newSetting);
                CommonPacketHandler.INSTANCE.sendToServer(new MultiblockCommandActivateMessage(master, newSetting));
			}
		}
		else if(button.id >= 2 && button.id <= 4) {
			switch(button.id) {
			case 4:
				
				break;
			default:
				
				break;
			}
		}
		else if(button.id == 5) {
            
		}
	}
	
	private String getFormattedOutputString() {
		//float number = null;
		//return StaticUtils.Strings.formatRF(number) + "/t";
		return null;
	}
	
	
	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(Reference.GUI_DIRECTORY + "BasicBackground.png");
	}

	
	

}
