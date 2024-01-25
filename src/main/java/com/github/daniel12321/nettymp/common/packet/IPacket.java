package com.github.daniel12321.nettymp.common.packet;

import io.netty.channel.Channel;

import java.util.UUID;

public interface IPacket {

    int getPacketId();

    UUID getRequestId();
    void setRequestId(UUID requestId);

    Channel getChannel();
    void setChannel(Channel channel);








    boolean DEBUG = true;
    static void debug(String msg) {
        if (DEBUG)
            System.out.println("[Netty Debug]    " + msg);
    }
}
