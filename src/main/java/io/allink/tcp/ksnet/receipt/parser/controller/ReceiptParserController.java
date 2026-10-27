package io.allink.tcp.ksnet.receipt.parser.controller;

import io.allink.tcp.ksnet.receipt.parser.dto.ParserRequest;
import io.allink.tcp.ksnet.receipt.parser.dto.ParserResponse;
import io.allink.tcp.ksnet.receipt.parser.service.ReceiptParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/e-receipt")
@RequiredArgsConstructor
public class ReceiptParserController {

    private final ReceiptParserService receiptParserService;

    @PostMapping("/parser")
    public ResponseEntity<ParserResponse> parser(@RequestBody ParserRequest request) {
        log.info("영수증 수신 요청 - tagId={}, partnerReqUuid={}",
                request.getTagId(), request.getPartnerReqUuid());

        ParserResponse response = receiptParserService.process(request);

        if ("NOT_OK".equals(response.getResultCode())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
