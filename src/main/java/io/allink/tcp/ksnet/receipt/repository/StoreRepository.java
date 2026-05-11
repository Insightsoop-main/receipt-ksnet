package io.allink.tcp.ksnet.receipt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.allink.tcp.ksnet.receipt.model.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

  @Query(value = """
      select
        store_name,
        store_uid,
        addr1,
        addr2,
        mobile,
        business_no,
        ceo_name
      from store st
      where business_no = ?1
        and exists( select 1 from merchant_tag mtag where mtag.store_uid = st.store_uid and mtag.device_id = ?2 and merchant_group_id LIKE 'KSNET%')
      """, nativeQuery = true)
  List<Store> findAllByBusinessNoAndDeviceId(String businessNo, String deviceId);

}
