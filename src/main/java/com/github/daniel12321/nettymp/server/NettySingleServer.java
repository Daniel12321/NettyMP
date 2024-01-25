package com.github.daniel12321.nettymp.server;

import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import io.netty.channel.Channel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NettySingleServer extends NettyServerBase {

    private Channel client;

    public NettySingleServer(int port) {
        super(port);
    }

    public void setClient(Channel client) {
        this.client = client;
    }

    @Override
    public synchronized void close() {
        super.close();

        try {
            this.client.close().sync();
        } catch (InterruptedException exc) {
            LOGGER.log(Level.SEVERE, "Failed to close netty client channels!");
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    public <T extends IPacket> void send(T object, NettyRequestHandler<T> handler) {
        this.send(this.client, object, handler);
    }
}
