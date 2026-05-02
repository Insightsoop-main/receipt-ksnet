package io.allink.tcp.ksnet.receipt.config;

import io.allink.tcp.ksnet.receipt.server.ReceiptServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
@RequiredArgsConstructor
public class ServerConfiguration {
    @Value("${server.netty.port}")
    private int port;

    @Bean
    public ServerBootstrap serverBootstrap(ReceiptServerChannelInitializer payServerChannelInitializer) {
        // ServerBootstrap: 서버 설정을 도와주는 class
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(payServerChannelInitializer);

        return bootstrap;
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        // incoming connection 관리가 필요하면 변수 지정
        // return new NioEventLoopGroup(bossCount);
        return new NioEventLoopGroup();
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        // 트래픽 관리가 필요하면 변수 지정
        // return new NioEventLoopGroup(workerCount);
        return new NioEventLoopGroup();
    }

    // IP 소켓 주소(IP 주소, Port 번호)를 구현
    // 도메인 이름으로 객체 생성 가능
    @Bean
    public InetSocketAddress inetSocketAddress() {
        return new InetSocketAddress(port);
    }
}
