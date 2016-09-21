package com.dora_goon.bigrf.handler;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import com.dora_goon.bigrf.handler.message.MultiblockCommandActivateMessage;
import com.dora_goon.bigrf.handler.message.MultiblockItemPortChangeDirectionMessage;
import com.dora_goon.bigrf.handler.message.MultiblockUpdateMessage;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;

public class CommonPacketHandler {

	/*
	 * Naming Convention:
	 *  Client >> Server
	 *   [Machine|TileEntity]ChangeMessage -- a full state change message (for large/batch commits)
	 *   [Machine|TileEntity]Change[Datum]Message -- a client request to change [Datum]
	 *  
	 *  Server >> Client
	 *   [Machine|TileEntity]UpdateMessage  -- a full state update
	 *   [Machine|TileEntity]Update[Datum]Message -- an update for only [Datum]
	 *   
	 *   Generic Format: [Machine|TileEntity][Operation][Type]Message
	 */
	
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL.toLowerCase());

    /**
     * Initialize the messages. Note that all messages (server>client and client>server)
     * must be initialized on _both_ the client and the server.
     */
    // Be careful not to reference any client code in your message handlers, such as WorldClient!
    public static void init() {
    	// Server >> Client Messages
    	LogHelper.info("Registering Server >> Client Messages");
        INSTANCE.registerMessage(MultiblockUpdateMessage.Handler.class, MultiblockUpdateMessage.class, 11, Side.CLIENT);
   
        // Client >> Server Messages
        LogHelper.info("Registering Client >> Server Messages");
    	INSTANCE.registerMessage(MultiblockCommandActivateMessage.Handler.class, MultiblockCommandActivateMessage.class, 0, Side.SERVER);
    	INSTANCE.registerMessage(MultiblockItemPortChangeDirectionMessage.Handler.class, MultiblockItemPortChangeDirectionMessage.class, 18, Side.SERVER);
    }
}
