package com.asterisk.network.socks5.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/26.
 */
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest>{


    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        LOGGER.info("target server:{}, {}, {}",msg.type(),msg.dstAddr(),msg.dstPort());
        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            LOGGER.trace("ready to connect server....");
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new DestClient(ctx));
                        }
                    });
            LOGGER.trace("start to connect server ....");
            final ChannelFuture channelFuture = bootstrap.connect(msg.dstAddr(),msg.dstPort());
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    LOGGER.trace("connect success !");
                    ctx.pipeline().addLast(new Client2Dest(channelFuture));
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,Socks5AddressType.IPv4);
                    ctx.writeAndFlush(commandResponse);
                } else {
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE,Socks5AddressType.IPv4);
                    ctx.writeAndFlush(commandResponse);
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private static class DestClient extends ChannelInboundHandlerAdapter {


        private ChannelHandlerContext context;

        DestClient(ChannelHandlerContext context) {
            this.context = context;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            context.writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            context.channel().close();
        }


    }

    private static class Client2Dest extends ChannelInboundHandlerAdapter {
        private ChannelFuture future;

        Client2Dest(ChannelFuture future) {
            this.future = future;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            future.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            future.channel().close();
        }
    }
}
