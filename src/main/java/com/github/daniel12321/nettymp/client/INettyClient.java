package com.github.daniel12321.nettymp.client;

import com.github.daniel12321.nettymp.common.INettyListener;
import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;

import java.util.UUID;

public interface INettyClient {

    /**
     * Starts the netty client, if it has not started yet.
     */
    void startAsync();

    /**
     * Closed the netty channel, if open.
     */
    void close();

    /**
     * Registers a {@link INettyListener} listener for a specific type of {@link IPacket}.
     *
     * @param clazz The class of {@link IPacket} the listener should trigger for.
     * @param listener The {@link INettyListener} listener.
     */
    <T extends IPacket> void addListener(Class<T> clazz, INettyListener<T> listener);

    /**
     * Removes the specified listener.
     *
     * @param listener The {@link INettyListener} to remove.
     */
    <T extends IPacket> void removeListener(INettyListener<T> listener);

    /**
     * Removes all listener listening to a specific {@link IPacket}.
     *
     * @param clazz The class of the {@link IPacket}.
     */
    <T extends IPacket> void removeListeners(Class<T> clazz);

    /**
     * Will send the packet to either the server or a client.
     *
     * @param object The {@link IPacket} object.
     * @param handler The {@link NettyRequestHandler} for the object that is called after the packet returns.
     * @throws IllegalStateException if called on a multi-client server
     */
    <T extends IPacket> void send(T object, NettyRequestHandler<T> handler) throws IllegalStateException;

    /**
     * Nullable
     *
     * Gets the {@link NettyRequestHandler} for a specific {@link IPacket} object.
     * Returns NULL when no event matching the uuid was found.
     * It should only return NULL if the object was not send as a response, but instead was sent by the netty server as a request.
     *
     * @param uuid The {@link UUID} of the {@link IPacket} object.
     * @return The {@link NettyRequestHandler} belonging to the event or NULL.
     */
    NettyRequestHandler<? extends IPacket> getAndRemoveHandler(UUID uuid);

    /*    static INettyClient of(*//*Configuration config*//*) {
        return new NettyClient("10.10.1.21", 8080);
//        return config.getBoolean("netty.enabled") ? new NettyClient(config.getString("netty.host"), config.getInt("netty.port")) : new EmptyNettyClient();
    }*/
}
