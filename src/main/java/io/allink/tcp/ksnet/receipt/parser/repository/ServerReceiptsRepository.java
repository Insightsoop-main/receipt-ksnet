package io.allink.tcp.ksnet.receipt.parser.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ServerReceiptsRepository {

    private final EntityManager em;

    /**
     * partnerReqUuid 기준 기존 uuid 조회 (중복 체크)
     */
    public String findUuidByPartnerReqUuid(String partnerReqUuid) {
        String sql = "SELECT uuid FROM server_receipts WHERE partner_req_uuid = :pru LIMIT 1";
        List<?> result = em.createNativeQuery(sql)
                .setParameter("pru", partnerReqUuid)
                .getResultList();
        return result.isEmpty() ? null : (String) result.get(0);
    }

    /**
     * server_receipts INSERT
     */
    @Transactional
    public void insert(String uuid, String partnerReqUuid, String source,
                       String storeUid, String posId, String tagId, String enc) {
        String sql = """
            INSERT INTO server_receipts
                (uuid, partner_req_uuid, partner_code, source, store_uid, pos_id, tag_id, enc, reg_date, is_claimed)
            VALUES
                (:uuid, :partnerReqUuid, 'KSNET-POS', :source, :storeUid, :posId, :tagId, :enc, now(), false)
            """;

        em.createNativeQuery(sql)
                .setParameter("uuid", uuid)
                .setParameter("partnerReqUuid", partnerReqUuid)
                .setParameter("source", source)
                .setParameter("storeUid", storeUid != null ? storeUid : "")
                .setParameter("posId", posId != null ? posId : "")
                .setParameter("tagId", tagId != null ? tagId : "")
                .setParameter("enc", enc != null ? enc : "EUC-KR")
                .executeUpdate();
    }
}
