package com.asterisk.network.socks5.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/26.
 */
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest>{

    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5InitialRequestHandler.class);


    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        LOGGER.info("initialized ss5 connection...{}",msg);
        if (msg.decoderResult().isFailure()) {
            LOGGER.info("not ss5 protocol...");
            ctx.fireChannelRead(msg);
        } else {
            if (msg.version().equals(SocksVersion.SOCKS5)) {
                Socks5InitialResponse response = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
                ctx.writeAndFlush(response);
            }
        }
    }
}
