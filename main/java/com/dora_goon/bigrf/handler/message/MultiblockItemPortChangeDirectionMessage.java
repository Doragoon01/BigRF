package com.dora_goon.bigrf.handler.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import com.dora_goon.bigrf.handler.message.TileMessageServer;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockItemPort;

public class MultiblockItemPortChangeDirectionMessage extends TileMessageServer<TileEntityMultiblockItemPort> {
	private boolean newSetting;
	public MultiblockItemPortChangeDirectionMessage() { super(); newSetting = true; }
	public MultiblockItemPortChangeDirectionMessage(TileEntityMultiblockItemPort port, boolean inlet) {
		super(port);
		newSetting = inlet;
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
	
	public static class Handler extends TileMessageServer.Handler<MultiblockItemPortChangeDirectionMessage, TileEntityMultiblockItemPort> {
		@Override
		protected IMessage handle(MultiblockItemPortChangeDirectionMessage message,
				MessageContext ctx, TileEntityMultiblockItemPort te) {
			te.setInlet(message.newSetting);
			return null;
		}

		@Override
		protected TileEntityMultiblockItemPort getImpl(TileEntity te) {
			if(te instanceof TileEntityMultiblockItemPort) {
				return (TileEntityMultiblockItemPort)te;
			}
			return null;
		}
	}
}
