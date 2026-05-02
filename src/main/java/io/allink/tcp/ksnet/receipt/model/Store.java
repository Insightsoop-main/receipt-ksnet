package io.allink.tcp.ksnet.receipt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store")
public class Store {

    @Id
    @Column(name = "store_uid", length = 36, nullable = false)
    private String storeUid;

    @Column(name = "store_name", nullable = false)
    private String storeName;

/*
    @Column(name = "store_type", length = 255)
    private String storeType = "DEF";
*/

/*
    @Column(name = "zone_code", length = 255)
    private String zoneCode;
*/

    @Column(name = "addr1")
    private String addr1;

    @Column(name = "addr2")
    private String addr2;
/*
    @CreationTimestamp
    @Column(name = "reg_date", updatable = false)
    private LocalDateTime regDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "franchise_code", length = 30)
    private String franchiseCode;

    @Column(name = "map_url", length = 255)
    private String mapUrl;

    @Column(name = "lat", length = 20)
    private String lat;

    @Column(name = "lon", length = 20)
    private String lon;

    @Column(name = "tel", length = 15)
    private String tel;

    @Column(name = "manager_name", length = 30)
    private String managerName;

    @Column(name = "site_link", length = 255)
    private String siteLink;

    @Column(name = "receipt_width_inch", length = 255)
    private String receiptWidthInch;*/

    /*@Column(name = "work_type", length = 100)
    private String workType;*/

    @Column(name = "mobile", length = 15)
    private String mobile;

    @Column(name = "business_no", length = 30)
    private String businessNo;

    @Column(name = "ceo_name", length = 30)
    private String ceoName;
/*
    @Column(name = "business_type", length = 255)
    private String businessType;

    @Column(name = "event_type", length = 255)
    private String eventType;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "partner_login_id", length = 50)
    private String partnerLoginId;

    @Column(name = "partner_login_pword", length = 255)
    private String partnerLoginPword;*/
}

