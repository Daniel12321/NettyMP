package com.github.daniel12321.nettymp.common.packet;

import java.util.UUID;

public class InvalidPacket extends BasePacket {

    public InvalidPacket() {
        super(0, new UUID(0, 0));
    }
}
