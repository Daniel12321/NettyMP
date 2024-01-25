package com.github.daniel12321.nettymp.server;

import com.github.daniel12321.nettymp.common.INettyListener;
import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.github.daniel12321.nettymp.common.timing.TaskScheduler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NettyServerBase implements INettyServer {

    protected static final Logger LOGGER = Logger.getLogger("NettyServer");

    private final int port;
    protected final Map<Class<? extends IPacket>, List<INettyListener<? extends IPacket>>> listeners;
    protected final Map<UUID, NettyRequestHandler<? extends IPacket>> handlers;

    protected volatile boolean started;
    private Channel channel;

    public NettyServerBase(int port) {
        this.port = port;
        this.listeners = new HashMap<>();
        this.handlers = new HashMap<>();
        this.started = false;
    }

    @Override
    public void startAsync() {
        synchronized (this) {
            if (this.started)
                return;
        }

        new Thread(this::start, "Netty Thread").start();
    }

    private void start() {
        LOGGER.log(Level.INFO, "Starting the netty server...");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            this.channel = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(this))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(this.port).sync().channel();

            synchronized (this) {
                this.started = true;
            }

            LOGGER.log(Level.INFO, "Started the netty server successfully!");

            this.channel.closeFuture().sync();
        } catch (InterruptedException exc) {
            LOGGER.log(Level.SEVERE, "The netty server crashed!", exc);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            LOGGER.log(Level.INFO, "The netty server shut down gracefully!");
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            this.started = false;
        }

        if (this.channel != null)
            this.channel.close();

        LOGGER.log(Level.INFO, "Shutting down the netty server...");
    }

    @Override
    public <T extends IPacket> void send(Channel channel, T object, NettyRequestHandler<T> handler) {
        synchronized (this) {
            if (!this.started)
                return;
        }

        if (channel == null) {
            IPacket.debug("Failed to send IPacket: channel=null");
            return;
        }

        IPacket.debug("Sending IPacket: object=" + object);

        this.handlers.put(object.getRequestId(), handler);
        channel.writeAndFlush(object);

        TaskScheduler.getInstance().addTask(handler.getTimeout(), handler::handleTimeout);
    }

    @Override
    public <T extends IPacket> void addListener(Class<T> clazz, INettyListener<T> listener) {
        this.listeners.computeIfAbsent(clazz, c -> new LinkedList<>()).add(listener);
    }

    @Override
    public <T extends IPacket> void removeListener(INettyListener<T> listener) {
        this.listeners.values().forEach(l -> l.remove(listener));
    }

    @Override
    public <T extends IPacket> void removeListeners(Class<T> clazz) {
        this.listeners.remove(clazz);
    }

    @Override
    public NettyRequestHandler<? extends IPacket> getAndRemoveHandler(UUID uuid) {
        return this.handlers.remove(uuid);
    }

    /**
     * Used to trigger all {@link INettyListener}'s to an incoming {@link IPacket}.
     *
     * @param object The {@link IPacket} object.
     */
    public <T extends IPacket> void callListeners(T object) {
        this.listeners.get(object.getClass()).forEach(l -> ((INettyListener<T>) l).onEvent(object));
    }
}
