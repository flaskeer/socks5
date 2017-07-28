package com.asterisk.network.socks5.handler;

import com.asterisk.network.socks5.auth.PasswordAuth;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/27.
 */
public class Socks5PasswordAuthRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5PasswordAuthRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5PasswordAuthRequestHandler.class);

    private PasswordAuth passwordAuth;

    public Socks5PasswordAuthRequestHandler(PasswordAuth passwordAuth) {
        this.passwordAuth = passwordAuth;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5PasswordAuthRequest msg) throws Exception {
        LOGGER.info("username:{} ,password :{}", msg.username(), msg.password());
        if (passwordAuth.auth(msg.username(), msg.password())) {
            ProxyChannelTrafficShapingHandler.username(ctx, msg.username());
            Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS);
            ctx.writeAndFlush(response);
        } else {
            ProxyChannelTrafficShapingHandler.username(ctx, "unauthorized");
            Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
