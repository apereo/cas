package org.apereo.cas.audit.spi.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.ZonedDateTime;

/**
 * This is {@link AuditTrailEntity} that represents the audit table.
 * Schema is generated automatically.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@MappedSuperclass
@ToString
@SuperBuilder
@Accessors(chain = true)
@Getter
@Setter
public class AuditTrailEntity {

    /**
     * Audit table name.
     */
    public static final String AUDIT_TRAIL_TABLE_NAME = "COM_AUDIT_TRAIL";

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = -1;

    @Column(name = "AUD_USER")
    private String user;

    @Column(name = "AUD_CLIENT_IP")
    private String clientIp;

    @Column(name = "AUD_SERVER_IP")
    private String serverIp;

    private String resource;

    @Column(name = "AUD_ACTION")
    private String action;

    @Column(name = "APPLIC_CD")
    private String applicationCode;

    @Column(name = "AUD_USERAGENT")
    private String userAgent;

    @Column(name = "AUD_DATE", nullable = false)
    private ZonedDateTime recordDate;

    public AuditTrailEntity() {
        this.id = System.currentTimeMillis();
    }
}
