package io.allink.tcp.ksnet.receipt.parser.service;

import io.allink.tcp.ksnet.receipt.parser.dto.ParserRequest;
import io.allink.tcp.ksnet.receipt.parser.dto.ParserResponse;
import io.allink.tcp.ksnet.receipt.parser.repository.TmoneyReceiptsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptParserService {

    private final TmoneyReceiptsRepository tmoneyReceiptsRepository;

    public ParserResponse process(ParserRequest req) {
        // 필수값 검증
        if (req.getSource() == null || req.getSource().isBlank()) {
            return ParserResponse.error("4003", "source 누락");
        }
        if (req.getPartnerReqUuid() == null || req.getPartnerReqUuid().isBlank()) {
            return ParserResponse.error("4003", "partnerReqUuid 누락");
        }
        if (req.getTagId() == null || req.getTagId().isBlank()) {
            return ParserResponse.error("4003", "tagId 누락");
        }

        // 중복 체크 (partnerReqUuid 기준)
        String existingUuid = tmoneyReceiptsRepository.findUuidByPartnerReqUuid(req.getPartnerReqUuid());
        if (existingUuid != null) {
            log.info("중복 요청 - partnerReqUuid={}, existingUuid={}", req.getPartnerReqUuid(), existingUuid);
            return ParserResponse.duplicate(existingUuid);
        }

        // 신규 UUID 생성 및 저장
        String newUuid = UUID.randomUUID().toString();
        tmoneyReceiptsRepository.insert(
                newUuid,
                req.getPartnerReqUuid(),
                req.getSource(),
                req.getStoreUid(),
                req.getPosId(),
                req.getTagId(),
                req.getEnc()
        );

        log.info("영수증 저장 완료 - uuid={}, tagId={}, partnerReqUuid={}",
                newUuid, req.getTagId(), req.getPartnerReqUuid());

        return ParserResponse.ok(newUuid);
    }
}
