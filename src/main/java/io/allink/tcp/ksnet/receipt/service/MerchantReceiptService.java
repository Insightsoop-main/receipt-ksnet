package io.allink.tcp.ksnet.receipt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.allink.tcp.ksnet.receipt.protocol.KsnetMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MerchantReceiptService {

  @Autowired
  private EntityManager entityManager;

  public MerchantReceiptService() {
  }

  @Transactional
  public void insertWithJson(KsnetMessage receipt, String payload, String normalizedPayload) {

    String sql = "INSERT INTO merchant_receipt" +
        "(receipt_uuid, reg_date, merchant_store_id, payload, device_id, trx_id, van_type, normalized_payload)" +
        " VALUES (uuid_generate_v4(), now(), ?, ?::jsonb, ?, ?, 'KSNET_CAT', ?::jsonb)";

    Query query = entityManager.createNativeQuery(sql);

    query.setParameter(1, receipt.getMchNo());
    query.setParameter(2, payload);
    query.setParameter(3, receipt.getTermId());
    query.setParameter(4, "ksnet-" + (receipt.getTransDate() + "-" + receipt.getTrdUniKey()).trim());
    query.setParameter(5, normalizedPayload);
    query.executeUpdate();
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public boolean isExists(String trxId) {
    String sql = "select 1 from merchant_receipt where trx_id = ?";
    Query query = entityManager.createNativeQuery(sql);
    query.setParameter(1, "ksnet-" + trxId);
    try {
      query.getSingleResult();
    } catch (NoResultException e) {
      return false;
    }
    return true;
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public boolean isNotExistsMerchantTag(/*String merchantId, */String deviceId) {
    String sql = "select 1 from merchant_tag mt where mt.device_id = ? and mt.merchant_group_id LIKE 'KSNET%'";
    Query query = entityManager.createNativeQuery(sql);
//    query.setParameter(1, merchantId);
    query.setParameter(1, deviceId);
    try {
      query.getSingleResult();
    } catch (NoResultException e) {
      log.info("등록되지 않은 요청 deviceId = {}", /*merchantId,*/ deviceId);
      return true;
    }
    return false;
  }
}
