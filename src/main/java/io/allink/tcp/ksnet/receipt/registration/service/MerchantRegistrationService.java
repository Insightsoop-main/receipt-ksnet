package io.allink.tcp.ksnet.receipt.registration.service;

import io.allink.tcp.ksnet.receipt.registration.dto.RegistrationResponse;
import io.allink.tcp.ksnet.receipt.registration.dto.TagStoreRegistRequest;
import io.allink.tcp.ksnet.receipt.registration.repository.RcRegistrationRepository;
import io.allink.tcp.ksnet.receipt.registration.repository.StoresRepository;
import io.allink.tcp.ksnet.receipt.registration.repository.TerminalsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantRegistrationService {

    private static final String VALID_TERMINAL_TYPES = "KSNET-CAT|KSNET-POS";

    private final StoresRepository storesRepository;
    private final TerminalsRepository terminalsRepository;
    private final RcRegistrationRepository rcRegistrationRepository;

    public RegistrationResponse process(TagStoreRegistRequest req) {
        String type = req.getType();

        if ("D".equals(type)) {
            return processDelete(req);
        } else if ("U".equals(type)) {
            return processUpsert(req);
        } else {
            return RegistrationResponse.error("4221", "type 파라미터 값 오류 (U 또는 D)");
        }
    }

    private RegistrationResponse processUpsert(TagStoreRegistRequest req) {
        // 필수값 검증
        if (req.getTag() == null || req.getTag().getId() == null) {
            return RegistrationResponse.error("4003", "tag.id 누락");
        }
        if (req.getStore() == null || req.getStore().getBusinessNo() == null) {
            return RegistrationResponse.error("4003", "store.businessNo 누락");
        }
        if (req.getStore().getName() == null || req.getStore().getName().isBlank()) {
            return RegistrationResponse.error("4003", "store.name 누락");
        }
        if (req.getTag().getTerminalType() == null ||
            !req.getTag().getTerminalType().matches(VALID_TERMINAL_TYPES)) {
            return RegistrationResponse.error("4221", "terminalType 값 오류 (KSNET-CAT 또는 KSNET-POS)");
        }

        String tagId          = req.getTag().getId();
        String deviceId       = req.getTag().getDeviceId() != null ? req.getTag().getDeviceId() : tagId;
        String tagName        = req.getTag().getTagName();
        String terminalType   = req.getTag().getTerminalType();
        String merchantNumber = "KSNET-" + deviceId;  // 서버 자동 생성

        String businessNo = req.getStore().getBusinessNo();
        String storeName  = req.getStore().getName();
        String addr1      = req.getStore().getAddr1();
        String addr2      = req.getStore().getAddr2();
        String tel        = req.getStore().getTel();
        String ceoName    = req.getStore().getCeoName();

        log.info("가맹점 UPSERT - tagId={}, deviceId={}, businessNo={}, terminalType={}",
                tagId, deviceId, businessNo, terminalType);

        // 1. stores UPSERT
        UUID storeId = storesRepository.upsert(storeName, addr1, addr2, tel, businessNo, ceoName);

        // 2. terminals UPSERT
        UUID terminalId = terminalsRepository.upsert(storeId, tagId, tagName, deviceId, terminalType, merchantNumber);

        // 3. 재구축 코어에도 기록(dual-write). 실패해도 기존 등록은 성공으로 응답한다.
        try {
            rcRegistrationRepository.upsert(tagId, deviceId, tagName, terminalType,
                    businessNo, storeName, addr1, addr2, tel, ceoName);
        } catch (Exception e) {
            log.error("[rc] dual-write 실패 (기존 등록은 정상) tagId={}, deviceId={}", tagId, deviceId, e);
        }

        log.info("가맹점 저장 완료 - storeId={}, terminalId={}", storeId, terminalId);

        return RegistrationResponse.ok(storeId.toString(), terminalId.toString());
    }

    private RegistrationResponse processDelete(TagStoreRegistRequest req) {
        if (req.getTag() == null || req.getTag().getId() == null) {
            return RegistrationResponse.error("4003", "tag.id 누락");
        }

        String tagId = req.getTag().getId();
        log.info("가맹점 삭제(비활성화) - tagId={}", tagId);

        int updated = terminalsRepository.deactivate(tagId);
        if (updated == 0) {
            log.warn("삭제 대상 없음 - tagId={}", tagId);
        }

        try {
            rcRegistrationRepository.deactivate(tagId);
        } catch (Exception e) {
            log.error("[rc] dual-write 비활성 실패 (기존 처리는 정상) tagId={}", tagId, e);
        }

        return RegistrationResponse.deleted();
    }
}
