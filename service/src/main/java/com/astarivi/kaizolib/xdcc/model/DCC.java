package com.astarivi.kaizolib.xdcc.model;

import org.jetbrains.annotations.NotNull;

public record DCC(String filename, String ip, int port, long sizeBits, boolean ipv6) {
    @Override
    public @NotNull String toString() {
        return "DCC{" +
                "filename='" + filename + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", sizeBits=" + sizeBits +
                ", ipv6=" + ipv6 +
                '}';
    }
}
