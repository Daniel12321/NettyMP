package com.github.daniel12321.nettymp.common.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final Map<Integer, Class<? extends IPacket>> PACKETS = new HashMap<>();

    /**
     * Registers a packet with a specific id. Make sure to use the same id in both server and client side.
     * @param id The id of the {@link IPacket}.
     * @param packet The class of the {@link IPacket}.
     */
    public static void register(int id, Class<? extends IPacket> packet) {
        PACKETS.put(id, packet);
    }

    /**
     * Registers a packet with a specific id. Make sure to use the same id in both server and client side.
     *
     * @param packet The {@link IPacket}.
     */
    public static void register(IPacket packet) {
        register(packet.getPacketId(), packet.getClass());
    }

    /**
     * Nullable
     *
     * Gets the packet class for an id.
     *
     * @param id The id of the {@link IPacket}.
     */
    public static Class<? extends IPacket> getClass(int id) {
        return PACKETS.get(id);
    }

    // Register the InvalidPacket with packetId 0.
    static {
        register(0, InvalidPacket.class);
    }
}
