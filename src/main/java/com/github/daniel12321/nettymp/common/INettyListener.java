package com.github.daniel12321.nettymp.common;

import com.github.daniel12321.nettymp.common.packet.IPacket;

@FunctionalInterface
public interface INettyListener<T extends IPacket> {

    void onEvent(T event);
}
