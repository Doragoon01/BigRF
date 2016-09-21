package com.dora_goon.bigrf.handler.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;

import com.dora_goon.bigrf.multiblock.MultiblockBoiler;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.utility.LogHelper;
import com.dora_goon.bigrf.handler.message.baseMultiblockMessageClient;

public class MultiblockUpdateMessage extends baseMultiblockMessageClient {
	ByteBuf data;

	public MultiblockUpdateMessage(MultiblockMasterBase master) {
		super(master);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		data = buf.readBytes(buf.readableBytes());
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		master.serialize(buf);
	}
	
	public static class Handler extends baseMultiblockMessageClient.Handler<MultiblockUpdateMessage> {
		@Override
		protected IMessage handleMessage(MultiblockUpdateMessage message, MessageContext ctx, Object master) {
			
			if (master instanceof MultiblockCell){
				((MultiblockCell) master).deserialize(message.data);
			}else if(master instanceof MultiblockTurbine){
				((MultiblockTurbine) master).deserialize(message.data);
			}else if(master instanceof MultiblockBoiler){
				((MultiblockBoiler) master).deserialize(message.data);	
			}else{
				//TODO finish adding types of multiblocks
			}
			
			return null;
		}
	}
}