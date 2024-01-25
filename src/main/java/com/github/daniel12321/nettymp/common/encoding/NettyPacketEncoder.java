package com.github.daniel12321.nettymp.common.encoding;

import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NettyPacketEncoder extends MessageToByteEncoder<IPacket> {

    private static final Gson GSON = new Gson();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext context, IPacket event, ByteBuf out) {
        String objectString = GSON.toJson(event);

        IPacket.debug("Encoded message: packedId=" + event.getPacketId() + ", requestId=" + event.getRequestId() + ", class=" + event.getClass().getName() + ", object=" + objectString);

        byte[] objectBytes = objectString.getBytes(UTF_8);

        out.writeByte(0x04); // EOT byte
        out.writeInt(event.getPacketId());
        out.writeLong(event.getRequestId().getMostSignificantBits());
        out.writeLong(event.getRequestId().getLeastSignificantBits());
        out.writeInt(objectBytes.length);
        out.writeBytes(objectBytes);
        out.writeByte(0x04); // EOT byte
    }
}
