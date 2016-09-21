package com.dora_goon.bigrf.handler.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;

import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;
import com.dora_goon.bigrf.utility.LogHelper;

public abstract class MultiblockMessageServer extends WorldMessageServer {
	MultiblockCell cell;
	
	protected MultiblockMessageServer() { super(); cell = null; }
	protected MultiblockMessageServer(MultiblockCell cell, CoordTriplet referenceCoord) {
		super(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.cell = cell;
	}
	protected MultiblockMessageServer(MultiblockCell cell) {
		this(cell, cell.getReferenceCoord());
	}
	
	public static abstract class Handler<M extends MultiblockMessageServer> extends WorldMessageServer.Handler<M> {
		protected abstract IMessage handleMessage(M message, MessageContext ctx, Object master);
		//TODO something here also
		@Override
		protected IMessage handleMessage(M message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityMultiblockPartBase) {
				LogHelper.info("Sending Server Message");
				Object master = ((TileEntityMultiblockPartBase)te).getMaster();
				if(master != null) {
					return handleMessage(message, ctx, master);
				}
				else {
					BeefCoreLog.error("Received ReactorMessageServer for a reactor part @ %d, %d, %d which has no attached reactor", te.xCoord, te.yCoord, te.zCoord);
				}
			}
			else {
				BeefCoreLog.error("Received ReactorMessageServer for a non-reactor-part block @ %d, %d, %d", message.x, message.y, message.z);
			}
			return null;
		}
	}
}
