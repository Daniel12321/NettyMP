package com.github.daniel12321.nettymp.server;

import com.github.daniel12321.nettymp.common.NettyHandler;
import com.github.daniel12321.nettymp.common.encoding.NettyPacketDecoder;
import com.github.daniel12321.nettymp.common.encoding.NettyPacketEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerBase server;

    public NettyServerInitializer(NettyServerBase server) {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder", new NettyPacketDecoder());
        pipeline.addLast("encoder", new NettyPacketEncoder());
        pipeline.addLast("handler", new NettyHandler(this.server));
    }
}
