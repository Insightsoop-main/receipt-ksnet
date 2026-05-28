package io.allink.tcp.ksnet.receipt.service;


import java.util.List;

import io.allink.tcp.ksnet.receipt.model.Store;
import io.allink.tcp.ksnet.receipt.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store findAllByBusinessNoAndDeviceId(String businessNo, String deviceId) {
        // Supabase는 business_number를 하이픈 없이 저장 (구 RDS는 하이픈 있었으나 Supabase는 없음)
        final List<Store> stores = storeRepository.findAllByBusinessNoAndDeviceId(businessNo, deviceId);
        if (stores.isEmpty()) {
            return null;
        }
        return stores.get(0);
    }
}
