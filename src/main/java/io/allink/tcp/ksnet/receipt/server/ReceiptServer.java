package io.allink.tcp.ksnet.receipt.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptServer {
    private final ServerBootstrap serverBootstrap;
    private final InetSocketAddress port;
    private Channel serverChannel;

    @PostConstruct
    public void init() {
        log.info("init...");
    }

    public void start() {
        try {
            ChannelFuture serverChannelFuture = serverBootstrap.bind(port).sync();
            log.info("🚀 KocessReceipt TCP 서버가 포트 " + port.getPort() + "에서 실행 중...");
            serverChannel = serverChannelFuture.channel().closeFuture().sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel.parent().closeFuture();
        }
    }
}
