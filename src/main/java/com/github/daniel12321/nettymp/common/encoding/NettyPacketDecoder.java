package com.github.daniel12321.nettymp.common.encoding;

import com.github.daniel12321.nettymp.common.packet.IPacket;
import com.github.daniel12321.nettymp.common.packet.InvalidPacket;
import com.github.daniel12321.nettymp.common.packet.PacketRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class NettyPacketDecoder extends ReplayingDecoder<Void> {

    private static final IPacket INVALID_OBJECT = new InvalidPacket();

    private static final Gson GSON = new Gson();
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {

        // Check if there are at least 26 bytes: EOT(1) + int(4) packetId + UUID(16) requestId + int(4) objectLength + EOT(1)
        if (in.readableBytes() < 26)
            return;

        // If first byte isnt an EOT byte, read till it finds an EOT byte.
        // This should stop before the opening EOT byte of a new packet, as the end of a packet also has an EOT byte
        if (in.readByte() != 0x04) {
            int byteCount = 0;
            while (in.isReadable() && in.readByte() != 0x04)
                byteCount++;

            out.add(INVALID_OBJECT);
            IPacket.debug("Skipped " + byteCount + " invalid bytes.");
            return;
        }

        // Read the packedId, the requestId, and the json object length
        int packetId = in.readInt();
        UUID requestId = new UUID(in.readLong(), in.readLong());
        int objectLength = in.readInt();

        // Check if the full packet is readable (+ EOT byte)
        if (in.readableBytes() < objectLength + 1)
            return;

        // Read the object body from the buffer
        String objectString = in.readCharSequence(objectLength, UTF_8).toString();

        // Check if the last byte is an EOT byte
        if (in.readByte() != 0x04) {
            out.add(INVALID_OBJECT); // Adds a new object to the output list, so the invalid bytes are not retained in the buffer
            return;
        }

        // Get the packet's class from the PacketRegistry
        Class<? extends IPacket> clazz = PacketRegistry.getClass(packetId);

        // Check if the packetId exists
        if (clazz == null) {
            out.add(INVALID_OBJECT); // Adds a new object to the output list, so the invalid bytes are not retained in the buffer
            return;
        }

        IPacket.debug("Decoded message: packetId=" + packetId + ", requestId=" + requestId + ", class=" + clazz + ", object=" + objectString);

        // Convert to IPacket using GSON
        IPacket object;
        try {
            object = GSON.fromJson(objectString, clazz);

            if (object == null)
                throw new NullPointerException();

        } catch (JsonSyntaxException | NullPointerException ignored) {
            IPacket.debug("Received packet with invalid JSON!");

            object = INVALID_OBJECT;
        }

        object.setRequestId(requestId);
        object.setChannel(context.channel());
        out.add(object);
    }
}
