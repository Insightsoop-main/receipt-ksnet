package io.allink.tcp.ksnet.receipt.registration.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StoresRepository {

    private final EntityManager em;

    /**
     * business_number 기준 UPSERT
     *
     * stores 테이블에 business_number UNIQUE 제약이 없으므로
     * ON CONFLICT 대신 SELECT 후 INSERT/UPDATE 로 처리한다.
     *
     * @return 저장/수정된 store UUID
     */
    @Transactional
    public UUID upsert(String name, String address, String addressDetail,
                       String phone, String businessNumber, String ownerName) {

        UUID existingId = findIdByBusinessNumber(businessNumber);

        if (existingId != null) {
            update(existingId, name, address, addressDetail, phone, ownerName);
            return existingId;
        }
        return insert(name, address, addressDetail, phone, businessNumber, ownerName);
    }

    /**
     * business_number 로 기존 store 조회 (가장 먼저 등록된 건 기준)
     */
    private UUID findIdByBusinessNumber(String businessNumber) {
        String sql = """
            SELECT id FROM stores
            WHERE business_number = :businessNumber
            ORDER BY created_at
            LIMIT 1
            """;

        List<?> result = em.createNativeQuery(sql)
                .setParameter("businessNumber", businessNumber)
                .getResultList();

        return result.isEmpty() ? null : (UUID) result.get(0);
    }

    private void update(UUID id, String name, String address, String addressDetail,
                        String phone, String ownerName) {
        String sql = """
            UPDATE stores SET
                name           = :name,
                address        = :address,
                address_detail = :addressDetail,
                phone          = :phone,
                owner_name     = :ownerName,
                is_active      = true,
                updated_at     = now()
            WHERE id = :id
            """;

        em.createNativeQuery(sql)
                .setParameter("id", id)
                .setParameter("name", name)
                .setParameter("address", address)
                .setParameter("addressDetail", addressDetail != null ? addressDetail : "")
                .setParameter("phone", phone != null ? phone : "")
                .setParameter("ownerName", ownerName != null ? ownerName : "")
                .executeUpdate();
    }

    private UUID insert(String name, String address, String addressDetail,
                        String phone, String businessNumber, String ownerName) {
        String sql = """
            INSERT INTO stores (id, name, address, address_detail, phone, business_number, owner_name, is_active)
            VALUES (gen_random_uuid(), :name, :address, :addressDetail, :phone, :businessNumber, :ownerName, true)
            RETURNING id
            """;

        return (UUID) em.createNativeQuery(sql)
                .setParameter("name", name)
                .setParameter("address", address)
                .setParameter("addressDetail", addressDetail != null ? addressDetail : "")
                .setParameter("phone", phone != null ? phone : "")
                .setParameter("businessNumber", businessNumber)
                .setParameter("ownerName", ownerName != null ? ownerName : "")
                .getSingleResult();
    }
}
