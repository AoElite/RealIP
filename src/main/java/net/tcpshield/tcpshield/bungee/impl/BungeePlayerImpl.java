package net.tcpshield.tcpshield.bungee.impl;

import io.netty.channel.AbstractChannel;
import net.md_5.bungee.api.connection.PendingConnection;
import net.tcpshield.tcpshield.ReflectionUtils;
import net.tcpshield.tcpshield.abstraction.IPlayer;
import net.tcpshield.tcpshield.exception.IPModificationFailureException;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class BungeePlayerImpl implements IPlayer {

    private final PendingConnection pendingConnection;
    private String ip;

    public BungeePlayerImpl(PendingConnection pendingConnection) {
        this.pendingConnection = pendingConnection;
        this.ip = pendingConnection.getAddress().getAddress().getHostAddress();
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public void setIP(InetSocketAddress ip) throws IPModificationFailureException {
        this.ip = ip.getAddress().getHostAddress();

        try {
            Object channelWrapper = ReflectionUtils.getObjectInPrivateField(pendingConnection, "ch");
            Object channel = ReflectionUtils.getObjectInPrivateField(channelWrapper, "ch");

            try {
                Field socketAddressField = ReflectionUtils.searchFieldByClass(channelWrapper.getClass(), SocketAddress.class);
                ReflectionUtils.setFinalField(channelWrapper, socketAddressField, ip);
            } catch (IllegalArgumentException ignored) {
                // Some BungeeCord versions, notably those on 1.7 (e.g. zBungeeCord) don't have an SocketAddress field in the ChannelWrapper class
            }

            ReflectionUtils.setFinalField(channel, ReflectionUtils.getPrivateField(AbstractChannel.class, "remoteAddress"), ip);
            ReflectionUtils.setFinalField(channel, ReflectionUtils.getPrivateField(AbstractChannel.class, "localAddress"), ip);
        } catch (Exception e) {
            throw new IPModificationFailureException(e);
        }
    }

    @Override
    public void disconnect() {
        pendingConnection.disconnect();
    }
}
