package io.allink.tcp.ksnet.receipt.registration.repository;

import io.allink.tcp.ksnet.receipt.registration.entity.StoresEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StoresRepository {

    private final EntityManager em;

    /**
     * business_number 기준 UPSERT
     * @return 저장/수정된 store UUID
     */
    @Transactional
    public UUID upsert(String name, String address, String addressDetail,
                       String phone, String businessNumber, String ownerName) {
        String sql = """
            INSERT INTO stores (id, name, address, address_detail, phone, business_number, owner_name, is_active)
            VALUES (gen_random_uuid(), :name, :address, :addressDetail, :phone, :businessNumber, :ownerName, true)
            ON CONFLICT (business_number)
            DO UPDATE SET
                name           = EXCLUDED.name,
                address        = EXCLUDED.address,
                address_detail = EXCLUDED.address_detail,
                phone          = EXCLUDED.phone,
                owner_name     = EXCLUDED.owner_name,
                is_active      = true,
                updated_at     = now()
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
