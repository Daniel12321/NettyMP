package com.github.daniel12321.nettymp.common;

import io.netty.channel.Channel;

@FunctionalInterface
public interface INettyTarget {

    Channel getNettyChannel();
}
