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
        final List<Store> stores = storeRepository.findAllByBusinessNoAndDeviceId(
            businessNo.replaceAll("(\\d{3})(\\d{2})(\\d{5})", "$1-$2-$3"), deviceId);
        if (stores.isEmpty()) {
            return null;
        }
        return stores.get(0);
    }
}
