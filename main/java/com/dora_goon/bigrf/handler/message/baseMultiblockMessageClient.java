package com.dora_goon.bigrf.handler.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;

import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;
import com.dora_goon.bigrf.utility.LogHelper;

public abstract class baseMultiblockMessageClient extends WorldMessageClient {
	protected MultiblockMasterBase master;
	
	protected baseMultiblockMessageClient() { super(); master = null; }
	protected baseMultiblockMessageClient(MultiblockMasterBase master, CoordTriplet referenceCoord) {
		super(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.master = master;
	}
	protected baseMultiblockMessageClient(MultiblockMasterBase master) {
		this(master, master.getReferenceCoord());
	}
	
	public static abstract class Handler<M extends baseMultiblockMessageClient> extends WorldMessageClient.Handler<M> {
		protected abstract IMessage handleMessage(M message, MessageContext ctx, Object master);
		//TODO stuff here
		@Override
		protected IMessage handleMessage(M message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityMultiblockPartBase) {
				//TODO make this work
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
