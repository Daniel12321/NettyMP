package com.github.daniel12321.nettymp.common;

import com.github.daniel12321.nettymp.client.INettyClient;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.github.daniel12321.nettymp.server.INettyServer;
import io.netty.channel.Channel;

import java.util.function.Consumer;

public class Request<T extends IPacket> {

    private final NettyRequestHandler.Builder<T> handler;

    private T packet;
    private Channel channel;

    public static <T extends IPacket> Request<T> builder(Class<T> clazz) {
        return new Request<T>(clazz);
    }

    public static <T extends IPacket> Request<T> builder(T packet) {
        return new Request<T>(packet).packet(packet);
    }

    private Request(Class<T> clazz) {
        this.handler = NettyRequestHandler.builder(clazz);
    }

    private Request(T packet) {
        this.handler = NettyRequestHandler.builder(packet);
    }

    public Request<T> packet(T packet) {
        this.packet = packet;
        return this;
    }

    public Request<T> target(Channel channel) {
        this.channel = channel;
        return this;
    }

    public Request<T> target(INettyTarget target) {
        this.channel = target.getNettyChannel();
        return this;
    }

    public Request<T> responseHandler(Consumer<T> handler) {
        this.handler.responseHandler(handler);
        return this;
    }

    public Request<T> timeoutHandler(Runnable handler) {
        this.handler.timeoutHandler(handler);
        return this;
    }

    public Request<T> timeout(long timeout) {
        this.handler.timeout(timeout);
        return this;
    }

    /**
     * Send the {@link IPacket} to a client of the server.
     * Requires a target channel being set.
     *
     * @param server The server through which to send the packet.
     */
    public void send(INettyServer server) {
        server.send(this.channel, this.packet, this.handler.build());
    }

    /**
     * Send the {@link IPacket} to the server that the {@link INettyClient} is connected to.
     * Does not require a channel to be set. If a channel is set, it will be ignored.
     *
     * @param client The client through which to send the packet.
     */
    public void send(INettyClient client) {
        client.send(this.packet, this.handler.build());
    }
}
