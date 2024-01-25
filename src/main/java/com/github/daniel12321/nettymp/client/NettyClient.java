package com.github.daniel12321.nettymp.client;

import com.github.daniel12321.nettymp.common.INettyListener;
import com.github.daniel12321.nettymp.common.NettyRequestHandler;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.github.daniel12321.nettymp.common.timing.TaskScheduler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyClient implements INettyClient {

    private static final Logger LOGGER = Logger.getLogger("NettyClient");

    private final String host;
    private final int port;

    private final Map<Class<? extends IPacket>, List<INettyListener<? extends IPacket>>> listeners;
    private final Map<UUID, NettyRequestHandler<? extends IPacket>> handlers;

    private Runnable onStart;
    private Channel channel;
    private volatile boolean starting;
    private volatile boolean started;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.listeners = new HashMap<>();
        this.handlers = new HashMap<>();

        this.channel = null;
        this.starting = false;
        this.started = false;
    }

    public NettyClient(String host, int port, Runnable onStart) {
        this(host, port);

        this.onStart = onStart;
    }

    @Override
    public void startAsync() {
        synchronized (this) {
            if (this.started || this.starting)
                return;

            this.starting = true;
        }

        new Thread(this::start, "Eagle Netty Thread").start();
    }

    private void start() {
        LOGGER.log(Level.INFO, "Starting the netty client...");

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new NettyClientInitializer(this))
                    .connect(this.host, this.port);

            try {
                this.channel = channelFuture.sync().channel();
                this.started = true;

                if (this.onStart != null) {
                    this.onStart.run();
                    this.onStart = null;
                }

                LOGGER.log(Level.INFO, "Started the netty client successfully!");
            } catch (Exception exc) {
                LOGGER.log(Level.WARNING, "Failed to connect to the netty server: " + exc.getMessage());
            }

            this.starting = false;
            if (this.started)
                this.channel.closeFuture().sync();

        } catch (InterruptedException exc) {
            LOGGER.log(Level.SEVERE, "The netty client crashed!", exc);
        } finally {
            workerGroup.shutdownGracefully();
            this.started = false;
            this.starting = false;

            LOGGER.log(Level.INFO, "The netty client shut down gracefully!");

//            TaskScheduler.getInstance().addTask(60_000, this::start);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (!this.started || this.channel == null || !this.channel.isActive())
                return;

            this.started = false;
        }

        LOGGER.log(Level.INFO, "Shutting down the netty client...");

        try {
            this.channel.close().sync();
        } catch (InterruptedException exc) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown the netty client properly!", exc);
        }
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
    public <T extends IPacket> void send(T object, NettyRequestHandler<T> handler) {
        synchronized (this) {
            if (!this.started || this.channel == null)
                return;
        }

        IPacket.debug("Sending IPacket: " + object);

        this.handlers.put(object.getRequestId(), handler);
        this.channel.writeAndFlush(object);

        TaskScheduler.getInstance().addTask(handler.getTimeout(), handler::handleTimeout);
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
