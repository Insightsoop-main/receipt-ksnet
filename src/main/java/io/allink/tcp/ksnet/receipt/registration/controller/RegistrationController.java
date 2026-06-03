package io.allink.tcp.ksnet.receipt.registration.controller;

import io.allink.tcp.ksnet.receipt.registration.dto.RegistrationResponse;
import io.allink.tcp.ksnet.receipt.registration.dto.TagStoreRegistRequest;
import io.allink.tcp.ksnet.receipt.registration.service.MerchantRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final MerchantRegistrationService registrationService;

    @PostMapping("/tag-store")
    public ResponseEntity<RegistrationResponse> register(@RequestBody TagStoreRegistRequest request) {
        log.info("가맹점 등록 요청 - type={}, tagId={}", request.getType(),
                request.getTag() != null ? request.getTag().getId() : "null");

        RegistrationResponse response = registrationService.process(request);

        if ("NOT_OK".equals(response.getResultCode())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
