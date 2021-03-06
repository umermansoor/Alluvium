package com.codeahoy.rocksolid.server.frontend;

import com.codeahoy.rocksolid.protocol.ProtocolBufferMessages;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PreDestroy;

/**
 *
 * TODO: Document and rename
 * @author umer
 */
@Configuration
class ServerConfiguration {
    @Value("${server.port:6677}")
    private int serverPort;

    @Autowired
    private ServerHandler serverHandler;

    private static final Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        // Wait until all threads are terminated.
        try {
            bossGroup.terminationFuture().sync();
            workerGroup.terminationFuture().sync();
        } catch (InterruptedException ie) {

        }
    }

    private ChannelInitializer<SocketChannel> channelInitializer() {
        ChannelInitializer<SocketChannel> bean = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new LoggingHandler(LogLevel.DEBUG),
                        new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4),
                        new ProtobufDecoder(ProtocolBufferMessages.Request.getDefaultInstance()),
                        new LengthFieldPrepender(4),
                        new ProtobufEncoder(),
                        serverHandler);
            }
        };

        return bean;
    }

    @Bean
    public ServerBootstrap socketServerBootstrap() {
        ServerBootstrap bean = new ServerBootstrap();
        bean.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .localAddress(serverPort)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(channelInitializer());

        try {
            bean.bind().sync();
        } catch (InterruptedException ie) {
        }

        logger.info("Rocksolid server listening on port " + serverPort + "");
        return bean;
    }

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    protected ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean() {
        ThreadPoolExecutorFactoryBean threadPoolExecutor = new ThreadPoolExecutorFactoryBean();
        threadPoolExecutor.setCorePoolSize(100);
        threadPoolExecutor.setMaxPoolSize(100);
        threadPoolExecutor.setQueueCapacity(100);
        return threadPoolExecutor;
    }

    @Bean(destroyMethod = "shutdown")
    protected ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);//TODO: size?
        taskScheduler.setThreadGroupName("taskScheduler");
        return taskScheduler;
    }



}
