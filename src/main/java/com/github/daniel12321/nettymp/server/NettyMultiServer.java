package com.github.daniel12321.nettymp.server;

import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;

public class NettyMultiServer extends NettyServerBase {

    public NettyMultiServer(int port) {
        super(port);
    }

    @Override
    public <T extends IPacket> void send(T object, NettyRequestHandler<T> handler) {
        throw new IllegalStateException("Called no-channel send method in multi-client server");
    }
}
