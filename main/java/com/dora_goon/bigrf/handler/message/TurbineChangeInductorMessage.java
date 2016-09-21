package com.dora_goon.bigrf.handler.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;

public class TurbineChangeInductorMessage extends TurbineMessageServer {
	boolean newSetting;
	public TurbineChangeInductorMessage() { super(); newSetting = true; }
	public TurbineChangeInductorMessage(MultiblockTurbine turbine, boolean newSetting) {
		super(turbine);
		this.newSetting = newSetting;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(newSetting);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		newSetting = buf.readBoolean();
	}
	
	public static class Handler extends TurbineMessageServer.Handler<TurbineChangeInductorMessage> {
		@Override
		protected IMessage handleMessage(TurbineChangeInductorMessage message,
				MessageContext ctx, MultiblockTurbine turbine) {
			turbine.setInductorEngaged(message.newSetting, true);
			return null;
		}
	}
}
