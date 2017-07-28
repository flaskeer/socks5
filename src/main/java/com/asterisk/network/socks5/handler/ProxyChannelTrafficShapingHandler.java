package com.asterisk.network.socks5.handler;

import com.asterisk.network.socks5.log.ProxyLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/26.
 */
public class ProxyChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {

    public static final String PROXY_TRAFFIC = "ProxyChannelTrafficShapingHandler";

    private long beginTime;

    private long endTime;

    private String username = "anonymous";

    private ProxyLogger proxyLogger;

    public ProxyChannelTrafficShapingHandler(long checkInterval,ProxyLogger proxyLogger) {
        super(checkInterval);
        this.proxyLogger = proxyLogger;
    }

    public static ProxyChannelTrafficShapingHandler get(ChannelHandlerContext ctx) {
        return (ProxyChannelTrafficShapingHandler) ctx.pipeline().get(PROXY_TRAFFIC);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        beginTime = System.currentTimeMillis();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        endTime = System.currentTimeMillis();
        proxyLogger.log(ctx);
        super.channelInactive(ctx);
    }

    public String getUsername() {
        return username;
    }

    public long beginTime() {
        return beginTime;
    }

    public long endTime() {
        return endTime;
    }

    static void username(ChannelHandlerContext ctx, String username) {
        get(ctx).username = username;
    }
}
