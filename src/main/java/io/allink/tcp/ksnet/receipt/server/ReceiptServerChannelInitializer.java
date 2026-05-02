package io.allink.tcp.ksnet.receipt.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReceiptServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ServerHandler serverHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ReadTimeoutHandler(20, TimeUnit.SECONDS)); // 20초 동안 서버 응답 없음 시 종료
        pipeline.addLast(new WriteTimeoutHandler(20, TimeUnit.SECONDS)); // 20초 동안 전송 실패 시 예외 발생
        pipeline.addLast(new TcpDecoder()); // TCP 자동 파싱
        pipeline.addLast(serverHandler);

    }
}
