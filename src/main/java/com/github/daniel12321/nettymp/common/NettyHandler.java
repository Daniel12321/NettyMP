package com.github.daniel12321.nettymp.common;

import com.github.daniel12321.nettymp.client.INettyClient;
import com.github.daniel12321.nettymp.client.NettyClient;
import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.github.daniel12321.nettymp.common.packet.InvalidPacket;
import com.github.daniel12321.nettymp.server.NettyServerBase;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

public class NettyHandler extends ChannelInboundHandlerAdapter {

    protected final INettyClient client;

    public NettyHandler(INettyClient client) {
        this.client = client;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) {
        InetSocketAddress address = (InetSocketAddress) context.channel().remoteAddress();
        IPacket.debug("Removed netty handler: " + address.getAddress().toString() + ":" + address.getPort());

        // TODO: Handle client closing
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        IPacket.debug("Netty channel is now active!");
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {
        IPacket packet = (IPacket) msg;
        if (packet instanceof InvalidPacket)
            return;

        NettyRequestHandler<? extends IPacket> handler = this.client.getAndRemoveHandler(packet.getRequestId());
        if (handler != null) {
            IPacket.debug("Found handler for IPacket: " + packet);
            handler.handle(packet);
        } else {
            IPacket.debug("Calling listeners for IPacket: " + packet);
            this.callListeners(packet);
            context.writeAndFlush(packet);
        }
    }

    private void callListeners(IPacket packet) {
        if (this.client instanceof NettyClient) {
            ((NettyClient) this.client).callListeners(packet);
        } else if (this.client instanceof NettyServerBase) {
            ((NettyServerBase) this.client).callListeners(packet);
        }
    }
}
