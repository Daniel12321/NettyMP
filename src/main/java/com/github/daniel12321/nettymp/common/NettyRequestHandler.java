package com.github.daniel12321.nettymp.common;

import com.github.daniel12321.nettymp.common.packet.IPacket;

import java.util.function.Consumer;

public class NettyRequestHandler<T extends IPacket> {

    private static final long DEFAULT_TIMEOUT = 2000;

    private final Class<T> clazz;
    private final Consumer<T> responseHandler; // Nullable
    private final Runnable timeoutHandler; // Nullable
    private final long timeout;

    private boolean handled;

    protected NettyRequestHandler(Class<T> clazz, Consumer<T> handler, Runnable timeoutHandler, long timeout) {
        this.clazz = clazz;
        this.responseHandler = handler; // Nullable
        this.timeoutHandler = timeoutHandler; // Nullable
        this.timeout = timeout;
    }

    public Class<T> getObjectClass() {
        return this.clazz;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void handle(IPacket event) {
        if (this.handled) return;
        this.handled = true;

        if (this.responseHandler != null && this.clazz.isAssignableFrom(event.getClass()))
            this.responseHandler.accept((T) event);
    }

    public void handleTimeout() {
        if (this.handled) return;
        this.handled = true;

        if (this.timeoutHandler != null)
            this.timeoutHandler.run();
    }

    public static <T extends IPacket> Builder<T> builder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    public static <T extends IPacket> Builder<T> builder(T object) {
        return builder((Class<T>) object.getClass());
    }

    public static <T extends IPacket> NettyRequestHandler<T> empty(Class<T> clazz) {
        return builder(clazz).build();
    }

    public static class Builder<T extends IPacket> {

        private final Class<T> objectClass;
        private Consumer<T> responseHandler; // Nullable
        private Runnable timeoutHandler; // Nullable
        private long timeout;

        public Builder(Class<T> objectClass) {
            this.objectClass = objectClass;
            this.timeout = DEFAULT_TIMEOUT;
        }

        public Builder<T> responseHandler(Consumer<T> handler) { // Nullable
            this.responseHandler = handler;
            return this;
        }

        public Builder<T> timeoutHandler(Runnable handler) { // Nullable
            this.timeoutHandler = handler;
            return this;
        }

        public Builder<T> timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public NettyRequestHandler<T> build() {
            return new NettyRequestHandler<>(this.objectClass, this.responseHandler, this.timeoutHandler, this.timeout);
        }
    }
}
