package io.allink.tcp.ksnet.receipt.registration.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "stores")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "phone")
    private String phone;

    @Column(name = "business_number", unique = true)
    private String businessNumber;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
