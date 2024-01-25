package com.github.daniel12321.nettymp.client;

import com.github.daniel12321.nettymp.common.NettyHandler;
import com.github.daniel12321.nettymp.common.encoding.NettyPacketDecoder;
import com.github.daniel12321.nettymp.common.encoding.NettyPacketEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyClient client;

    public NettyClientInitializer(NettyClient client) {
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new NettyPacketDecoder(), new NettyPacketEncoder(), new NettyHandler(this.client));
    }
}
