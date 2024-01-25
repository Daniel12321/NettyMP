package com.github.daniel12321.nettymp.common.packet;

import io.netty.channel.Channel;

import java.util.UUID;

public class BasePacket implements IPacket {

    protected transient final int packetId;

    protected transient UUID requestId;
    protected transient Channel channel;

    public BasePacket(int packetId) {
        this(packetId, UUID.randomUUID());
    }

    public BasePacket(int packetId, UUID requestId) {
        this.packetId = packetId;
        this.requestId = requestId;
    }

    @Override
    public int getPacketId() {
        return this.packetId;
    }

    @Override
    public UUID getRequestId() {
        return this.requestId;
    }

    @Override
    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
