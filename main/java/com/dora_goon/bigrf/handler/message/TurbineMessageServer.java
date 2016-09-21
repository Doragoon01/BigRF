package com.dora_goon.bigrf.handler.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPartBase;

import erogenousbeef.core.common.BeefCoreLog;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockMasterBase;

public class TurbineMessageServer extends WorldMessageServer {
	protected MultiblockTurbine turbine;
	
	protected TurbineMessageServer() { super(); turbine = null; }
	protected TurbineMessageServer(MultiblockTurbine turbine, CoordTriplet referenceCoord) {
		super(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.turbine = turbine;
	}
	protected TurbineMessageServer(MultiblockTurbine turbine) {
		this(turbine, turbine.getReferenceCoord());
	}
	
	public static abstract class Handler<M extends TurbineMessageServer> extends WorldMessageServer.Handler<M> {
		protected abstract IMessage handleMessage(M message, MessageContext ctx, MultiblockTurbine turbine);

		@Override
		protected IMessage handleMessage(M message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityMultiblockPartBase) {
				/*
				 // TODO fix the muliblock master system
				MultiblockMasterBase master = ((TileEntityMultiblockPartBase)te).getMaster();
				if(master != null) {
					return handleMessage(message, ctx, master);
				
				}
				else {
					BeefCoreLog.error("Received TurbineMessageServer for a turbine part @ %d, %d, %d which has no attached turbine", te.xCoord, te.yCoord, te.zCoord);
				} */
			}
			else {
				BeefCoreLog.error("Received TurbineMessageServer for a non-turbine-part block @ %d, %d, %d", message.x, message.y, message.z);
			}
			return null;
		}
	}
}
