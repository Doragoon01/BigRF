package com.dora_goon.bigrf.handler.message;

import net.minecraft.tileentity.TileEntity;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;
import com.dora_goon.bigrf.utility.LogHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;

public abstract class MultiblockMessageClient extends WorldMessageClient {
	protected MultiblockCell cell;
	
	protected MultiblockMessageClient() { super(); cell = null; }
	protected MultiblockMessageClient(MultiblockCell cell, CoordTriplet referenceCoord) {
		super(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.cell = cell;
	}
	protected MultiblockMessageClient(MultiblockCell cell) {
		this(cell, cell.getReferenceCoord());
	}
	
	public static abstract class Handler<M extends MultiblockMessageClient> extends WorldMessageClient.Handler<M> {
		protected abstract IMessage handleMessage(M message, MessageContext ctx, Object master);
		//TODO something here too
		@Override
		protected IMessage handleMessage(M message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityMultiblockPartBase) {
				Object master = ((TileEntityMultiblockPartBase)te).getMaster();
				if(master != null) {
					return handleMessage(message, ctx, master);
				}
				else {
					BeefCoreLog.error("Received ReactorMessageClient for a reactor part @ %d, %d, %d which has no attached reactor", te.xCoord, te.yCoord, te.zCoord);
				}
			}
			else {
				BeefCoreLog.error("Received ReactorMessageClient for a non-reactor-part block @ %d, %d, %d", message.x, message.y, message.z);
			}
			return null;
		}
	}
}
