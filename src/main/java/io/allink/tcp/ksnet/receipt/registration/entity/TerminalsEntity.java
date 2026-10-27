package io.allink.tcp.ksnet.receipt.registration.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "terminals")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "tag_id", nullable = false, unique = true)
    private String tagId;

    @Column(name = "name")
    private String name;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "merchant_group_id")
    private String merchantGroupId;

    @Column(name = "merchant_number")
    private String merchantNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "terminal_type", nullable = false)
    @Builder.Default
    private String terminalType = "POS";

    @Column(name = "terminal_number", nullable = false)
    @Builder.Default
    private String terminalNumber = "";
}
