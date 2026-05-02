package io.allink.tcp.ksnet.receipt.server;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceiptServerStartupTask implements ApplicationListener<ApplicationReadyEvent> {

    private final ReceiptServer receiptServer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        receiptServer.start();
    }
}
