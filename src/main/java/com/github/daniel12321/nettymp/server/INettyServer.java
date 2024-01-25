package com.github.daniel12321.nettymp.server;

import com.github.daniel12321.nettymp.client.INettyClient;
import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import io.netty.channel.Channel;

public interface INettyServer extends INettyClient {

    /**
     * Send the packet to a specific client.
     *
     * @param channel The client's {@link Channel} to send the data to.
     * @param object The {@link IPacket} object.
     * @param handler The {@link NettyRequestHandler} for the packet that is called after the packet returns.
     */
    <T extends IPacket> void send(Channel channel, T object, NettyRequestHandler<T> handler);
}
