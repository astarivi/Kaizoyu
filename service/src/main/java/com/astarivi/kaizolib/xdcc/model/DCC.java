package com.astarivi.kaizolib.xdcc.model;


public class DCC {
    private final String filename;
    private final String ip;
    private final int port;
    private final long sizeBits;
    private final boolean ipv6;

    public DCC(String filename, String ip, int port, long sizeBits, boolean ipv6) {
        this.filename = filename;
        this.ip = ip;
        this.port = port;
        this.sizeBits = sizeBits;
        this.ipv6 = ipv6;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getSizeBits() {
        return sizeBits;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    @Override
    public String toString() {
        return "DCC{" +
                "filename='" + filename + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", sizeBits=" + sizeBits +
                ", ipv6=" + ipv6 +
                '}';
    }
}
