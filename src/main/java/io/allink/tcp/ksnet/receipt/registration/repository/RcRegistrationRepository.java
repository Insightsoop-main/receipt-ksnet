package io.allink.tcp.ksnet.receipt.registration.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 재구축 코어(rc_stores / rc_terminals / rc_tags) 이중 기록.
 *
 * 기존 stores/terminals 기록은 그대로 두고 여기에 함께 쓴다(dual-write).
 * 전환이 끝나면 기존 기록만 떼어내면 된다.
 *
 * 원칙(docs/rebuild-plan.md):
 *   - 상점은 우리가 먼저 등록한다. KSNET 유입분은 "등록"이 아니라 매칭·보완이다.
 *   - 병합 키 우선순위: tag_id(우리 발급) → device_id → business_number
 *   - 단말 하나에 태그가 여러 장 붙을 수 있다(계산대·테이블 등)
 *   - terminalType이 어느 버퍼를 볼지 결정한다
 *       KSNET-CAT → receive_method=VAN    (merchant_receipt를 device_id로)
 *       KSNET-POS → receive_method=SERVER (server_receipts를 tag_id로)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RcRegistrationRepository {

    private final EntityManager em;

    @Transactional
    public void upsert(String tagId, String deviceId, String tagName, String terminalType,
                       String businessNo, String storeName, String addr1, String addr2,
                       String tel, String ceoName) {

        boolean isCat = "KSNET-CAT".equals(terminalType);
        String receiveMethod = isCat ? "VAN" : "SERVER";
        String partnerCode = isCat ? null : "KSNET-POS";

        // 1) 태그로 기존 단말 찾기 (우리가 선등록했다면 여기서 잡힌다)
        UUID terminalId = findTerminalIdByTag(tagId);

        // 2) 없으면 단말기번호로
        if (terminalId == null && deviceId != null && !deviceId.isBlank()) {
            terminalId = findTerminalIdByDevice(deviceId);
        }

        UUID storeId = terminalId != null ? findStoreIdByTerminal(terminalId) : null;

        // 3) 그래도 없으면 사업자번호로 상점 찾기
        if (storeId == null) {
            storeId = findStoreIdByBusinessNo(businessNo);
        }

        // 4) 상점이 없으면 생성 — 우리가 등록한 게 아니므로 검토 대기로 둔다
        if (storeId == null) {
            storeId = insertStore(businessNo, storeName, addr1, addr2, tel, ceoName);
            log.info("[rc] 상점 신규 생성(검토대기) storeId={}, businessNo={}", storeId, businessNo);
        } else {
            updateStore(storeId, storeName, addr1, addr2, tel, ceoName);
        }

        // 5) 단말 생성/보완
        if (terminalId == null) {
            terminalId = insertTerminal(storeId, tagName, deviceId, receiveMethod, partnerCode);
            log.info("[rc] 단말 신규 생성 terminalId={}, deviceId={}", terminalId, deviceId);
        } else {
            updateTerminal(terminalId, deviceId, receiveMethod, partnerCode);
        }

        // 6) 태그 배정 — 단말에 그냥 매단다. 이미 붙어 있는 다른 태그는 건드리지 않는다.
        //    규격의 type은 U(등록/수정)와 D(삭제)뿐이라 "교체"와 "추가"를 구분할 수 없다.
        //    추가로 해석하는 편이 손실이 없다:
        //      교체 상황이면 폐기된 옛 태그가 남을 뿐 아무도 찍지 않는다.
        //      추가 상황에서 교체로 처리하면 멀쩡히 붙어 있는 태그가 죽는다.
        //    실제로 한 단말에 태그를 두 장 쓰는 매장이 있다(2026-09-02 기준 6곳).
        //    교체가 필요하면 KSNET이 D로 옛 태그를 지우고 U로 새 태그를 보내면 된다.
        upsertTag(tagId, terminalId);

        log.info("[rc] UPSERT 완료 storeId={}, terminalId={}, tagId={}, method={}",
                storeId, terminalId, tagId, receiveMethod);
    }

    /** 삭제(type=D): 단말 비활성 + 태그 해제. 상점은 남긴다. */
    @Transactional
    public void deactivate(String tagId) {
        UUID terminalId = findTerminalIdByTag(tagId);

        em.createNativeQuery("UPDATE rc_tags SET status = 'retired', updated_at = now() WHERE tag_id = :tagId")
                .setParameter("tagId", tagId)
                .executeUpdate();

        if (terminalId != null) {
            em.createNativeQuery("""
                    UPDATE rc_terminals
                    SET status = 'inactive', is_active = false, updated_at = now()
                    WHERE id = :id
                    """)
                    .setParameter("id", terminalId)
                    .executeUpdate();
        }
        log.info("[rc] 비활성 처리 tagId={}, terminalId={}", tagId, terminalId);
    }

    // ── 조회 ──────────────────────────────────────────────────────

    private UUID findTerminalIdByTag(String tagId) {
        List<?> r = em.createNativeQuery("""
                SELECT terminal_id FROM rc_tags
                WHERE tag_id = :tagId AND status <> 'retired' AND terminal_id IS NOT NULL
                LIMIT 1
                """)
                .setParameter("tagId", tagId)
                .getResultList();
        return r.isEmpty() ? null : (UUID) r.get(0);
    }

    private UUID findTerminalIdByDevice(String deviceId) {
        List<?> r = em.createNativeQuery("SELECT id FROM rc_terminals WHERE device_id = :deviceId LIMIT 1")
                .setParameter("deviceId", deviceId)
                .getResultList();
        return r.isEmpty() ? null : (UUID) r.get(0);
    }

    private UUID findStoreIdByTerminal(UUID terminalId) {
        List<?> r = em.createNativeQuery("SELECT store_id FROM rc_terminals WHERE id = :id")
                .setParameter("id", terminalId)
                .getResultList();
        return r.isEmpty() ? null : (UUID) r.get(0);
    }

    private UUID findStoreIdByBusinessNo(String businessNo) {
        if (businessNo == null || businessNo.isBlank()) return null;
        List<?> r = em.createNativeQuery("""
                SELECT id FROM rc_stores
                WHERE business_number = :bn
                ORDER BY created_at
                LIMIT 1
                """)
                .setParameter("bn", businessNo)
                .getResultList();
        return r.isEmpty() ? null : (UUID) r.get(0);
    }

    // ── 상점 ──────────────────────────────────────────────────────

    private UUID insertStore(String businessNo, String name, String addr1, String addr2,
                             String tel, String ceoName) {
        // 네이티브 쿼리에 null을 바인딩하면 Postgres가 타입을 못 정한다 → 빈 문자열 + NULLIF
        return (UUID) em.createNativeQuery("""
                INSERT INTO rc_stores
                    (id, name, business_number, owner_name, address, address_detail, tel,
                     van_type, source, review_status, is_active)
                VALUES
                    (gen_random_uuid(), :name, NULLIF(:bn, ''), NULLIF(:ceo, ''),
                     NULLIF(:addr1, ''), NULLIF(:addr2, ''), NULLIF(:tel, ''),
                     'KSNET', 'ksnet', 'pending_review', true)
                RETURNING id
                """)
                .setParameter("name", nz(name, "미등록"))
                .setParameter("bn", nzs(businessNo))
                .setParameter("ceo", nzs(ceoName))
                .setParameter("addr1", nzs(addr1))
                .setParameter("addr2", nzs(addr2))
                .setParameter("tel", nzs(tel))
                .getSingleResult();
    }

    /**
     * 우리가 직접 등록한 상점(source='admin')은 이름·주소를 덮어쓰지 않고 빈 값만 채운다.
     * 영수증에 찍히는 값이라 품질을 우리가 통제한다는 원칙 때문이다.
     * KSNET이 만든 상점(source='ksnet')은 최신값으로 갱신한다.
     */
    private void updateStore(UUID id, String name, String addr1, String addr2,
                             String tel, String ceoName) {
        em.createNativeQuery("""
                UPDATE rc_stores SET
                    name           = CASE WHEN source = 'admin' THEN COALESCE(NULLIF(name, ''), :name)
                                          ELSE COALESCE(NULLIF(:name, ''), name) END,
                    owner_name     = CASE WHEN source = 'admin' THEN COALESCE(owner_name, :ceo)
                                          ELSE COALESCE(NULLIF(:ceo, ''), owner_name) END,
                    address        = CASE WHEN source = 'admin' THEN COALESCE(address, :addr1)
                                          ELSE COALESCE(NULLIF(:addr1, ''), address) END,
                    address_detail = CASE WHEN source = 'admin' THEN COALESCE(address_detail, :addr2)
                                          ELSE COALESCE(NULLIF(:addr2, ''), address_detail) END,
                    tel            = CASE WHEN source = 'admin' THEN COALESCE(tel, :tel)
                                          ELSE COALESCE(NULLIF(:tel, ''), tel) END,
                    updated_at     = now()
                WHERE id = :id
                """)
                .setParameter("id", id)
                .setParameter("name", nzs(name))
                .setParameter("ceo", nzs(ceoName))
                .setParameter("addr1", nzs(addr1))
                .setParameter("addr2", nzs(addr2))
                .setParameter("tel", nzs(tel))
                .executeUpdate();
    }

    // ── 단말 ──────────────────────────────────────────────────────

    private UUID insertTerminal(UUID storeId, String name, String deviceId,
                                String receiveMethod, String partnerCode) {
        return (UUID) em.createNativeQuery("""
                INSERT INTO rc_terminals
                    (id, store_id, name, device_id, receive_method, partner_code,
                     status, source, requested_at, is_active)
                VALUES
                    (gen_random_uuid(), :storeId, NULLIF(:name, ''), NULLIF(:deviceId, ''),
                     :method, NULLIF(:partner, ''),
                     'requested', 'ksnet', now(), true)
                RETURNING id
                """)
                .setParameter("storeId", storeId)
                .setParameter("name", nzs(name))
                .setParameter("deviceId", nzs(deviceId))
                .setParameter("method", receiveMethod)
                .setParameter("partner", nzs(partnerCode))
                .getSingleResult();
    }

    /**
     * 단말기번호는 KSNET이 발급하므로 항상 최신값으로 채운다.
     * status는 선등록(draft)일 때만 requested로 올리고, 이미 수신중(active)이면 건드리지 않는다.
     */
    private void updateTerminal(UUID id, String deviceId, String receiveMethod, String partnerCode) {
        em.createNativeQuery("""
                UPDATE rc_terminals SET
                    device_id      = COALESCE(NULLIF(:deviceId, ''), device_id),
                    receive_method = :method,
                    partner_code   = NULLIF(:partner, ''),
                    status         = CASE WHEN status = 'draft' THEN 'requested' ELSE status END,
                    requested_at   = COALESCE(requested_at, now()),
                    is_active      = true,
                    updated_at     = now()
                WHERE id = :id
                """)
                .setParameter("id", id)
                .setParameter("deviceId", nzs(deviceId))
                .setParameter("method", receiveMethod)
                .setParameter("partner", nzs(partnerCode))
                .executeUpdate();
    }

    // ── 태그 ──────────────────────────────────────────────────────

    private void upsertTag(String tagId, UUID terminalId) {
        em.createNativeQuery("""
                INSERT INTO rc_tags (id, tag_id, terminal_id, status, issued_at, assigned_at)
                VALUES (gen_random_uuid(), :tagId, :terminalId, 'assigned', now(), now())
                ON CONFLICT (tag_id) DO UPDATE SET
                    terminal_id = EXCLUDED.terminal_id,
                    status      = CASE WHEN rc_tags.status = 'active' THEN 'active' ELSE 'assigned' END,
                    assigned_at = COALESCE(rc_tags.assigned_at, now()),
                    updated_at  = now()
                """)
                .setParameter("tagId", tagId)
                .setParameter("terminalId", terminalId)
                .executeUpdate();
    }

    // ── util ──────────────────────────────────────────────────────

    private static String nz(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    /** SQL에서 NULLIF(:p,'')로 판정하기 위해 null을 빈 문자열로 정규화한다. */
    private static String nzs(String v) {
        return v == null ? "" : v;
    }
}
