package com.asterisk.network.socks5;

import com.asterisk.network.socks5.auth.PasswordAuth;
import com.asterisk.network.socks5.handler.*;
import com.asterisk.network.socks5.log.ProxyLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/26.
 */
public class ProxyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServer.class);


    private static void start(int port) throws InterruptedException {
        ProxyLogger logger = new ProxyLogger();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .option(ChannelOption.SO_TIMEOUT, 5000)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                                  ch.pipeline()
                                    .addLast(ProxyChannelTrafficShapingHandler.PROXY_TRAFFIC,new ProxyChannelTrafficShapingHandler(3000,logger))
                                    .addLast(new IdleStateHandler(3, 30, 0))
                                    .addLast(new LoggingHandler())
                                    .addLast(new ProxyIdleHandler())
                                    .addLast(Socks5ServerEncoder.DEFAULT)
                                    .addLast(new Socks5InitialRequestDecoder())
                                    .addLast(new Socks5InitialRequestHandler())
                                    .addLast(new Socks5PasswordAuthRequestDecoder())
                                    .addLast(new Socks5PasswordAuthRequestHandler(new PasswordAuth()))
                                    .addLast(new Socks5CommandRequestDecoder())
                                    .addLast(new Socks5CommandRequestHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
            LOGGER.info("server started ...");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }



    public static void main(String[] args) throws InterruptedException {
        int port = 11080;
        start(port);
    }
}
