package com.asterisk.network.socks5.log;

import com.asterisk.network.socks5.handler.ProxyChannelTrafficShapingHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/27.
 */
public class ProxyLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLogger.class);

    public void log(ChannelHandlerContext ctx) {
        ProxyChannelTrafficShapingHandler channelTrafficShapingHandler = ProxyChannelTrafficShapingHandler.get(ctx);
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        long readBytes = channelTrafficShapingHandler.trafficCounter().cumulativeReadBytes();
        long writeBytes = channelTrafficShapingHandler.trafficCounter().cumulativeWrittenBytes();
        LOGGER.info("{},{},{},{}:{},{}:{},{},{},{}",
                channelTrafficShapingHandler.getUsername(),
                channelTrafficShapingHandler.beginTime(),
                channelTrafficShapingHandler.endTime(),
                localAddress(),
                localAddress.getPort(),
                remoteAddress.getAddress().getHostAddress(),
                remoteAddress.getPort(),
                readBytes,
                writeBytes,
                (readBytes + writeBytes));
    }

    private static String localAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            LOGGER.error("Error when getting host ip address :<{}>",e.getMessage());
        }
        return"127.0.0.1";
    }

}
