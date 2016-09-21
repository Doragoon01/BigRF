package com.dora_goon.bigrf.handler.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockMasterBase;

import com.dora_goon.bigrf.handler.message.WorldMessageServer;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.interfaces.IActivateable;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;
import com.dora_goon.bigrf.utility.LogHelper;

/**
 * Send a "setActive" command to any IActivateable machine.
 * Currently used for multiblock reactors and turbines.
 * @see erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable
 * @author Erogenous Beef
 *
 */
public class MultiblockCommandActivateMessage extends WorldMessageServer {
	protected boolean setActive;
	public MultiblockCommandActivateMessage() { super(); setActive = true; }

	protected MultiblockCommandActivateMessage(CoordTriplet coord, boolean setActive) {
		super(coord.x, coord.y, coord.z);
		this.setActive = setActive;
	}

	public MultiblockCommandActivateMessage(IActivateable machine, boolean setActive) {
		this(machine.getReferenceCoord(), setActive);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(setActive);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		setActive = buf.readBoolean();
	}
	
	//TODO add casts for all the multiblock types
	public static class Handler extends WorldMessageServer.Handler<MultiblockCommandActivateMessage> {
		@Override
		protected IMessage handleMessage(MultiblockCommandActivateMessage message, MessageContext ctx, TileEntity te){
			MultiblockMasterBase master =  (MultiblockMasterBase) ((TileEntityMultiblockPartBase) te).getMaster();
			LogHelper.info("Activating: " + master);
			if(master instanceof IActivateable) {
				//IActivateable machine = (IActivateable)te;
				master.setActive(message.setActive);
			}
			else {
				BeefCoreLog.error("Received a MachineCommandActivateMessage for %d, %d, %d but found no activateable machine", message.x, message.y, message.z);
			}
			return null;
		}
	}
	
}
