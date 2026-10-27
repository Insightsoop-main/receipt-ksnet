package io.allink.tcp.ksnet.receipt.registration.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TerminalsRepository {

    private final EntityManager em;

    /**
     * tag_id 기준 UPSERT
     * @return 저장/수정된 terminal UUID
     */
    @Transactional
    public UUID upsert(UUID storeId, String tagId, String name,
                       String deviceId, String merchantGroupId, String merchantNumber) {
        String sql = """
            INSERT INTO terminals (id, store_id, tag_id, name, device_id, merchant_group_id, merchant_number, is_active, terminal_type, terminal_number)
            VALUES (gen_random_uuid(), :storeId, :tagId, :name, :deviceId, :merchantGroupId, :merchantNumber, true, 'POS', '')
            ON CONFLICT (tag_id)
            DO UPDATE SET
                store_id          = EXCLUDED.store_id,
                name              = EXCLUDED.name,
                device_id         = EXCLUDED.device_id,
                merchant_group_id = EXCLUDED.merchant_group_id,
                merchant_number   = EXCLUDED.merchant_number,
                is_active         = true,
                updated_at        = now()
            RETURNING id
            """;

        return (UUID) em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("tagId", tagId)
                .setParameter("name", name != null ? name : "")
                .setParameter("deviceId", deviceId)
                .setParameter("merchantGroupId", merchantGroupId)
                .setParameter("merchantNumber", merchantNumber)
                .getSingleResult();
    }

    /**
     * 소프트 삭제 (is_active = false)
     */
    @Transactional
    public int deactivate(String tagId) {
        String sql = """
            UPDATE terminals
            SET is_active = false, updated_at = now()
            WHERE tag_id = :tagId AND merchant_group_id LIKE 'KSNET%'
            """;

        return em.createNativeQuery(sql)
                .setParameter("tagId", tagId)
                .executeUpdate();
    }
}
