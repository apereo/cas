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
import java.util.Locale;

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

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "AUD_USER")
    private String user;

    @Column(name = "AUD_CLIENT_IP")
    private String clientIp;

    @Column(name = "AUD_SERVER_IP")
    private String serverIp;

    @Column(name = "AUD_RESOURCE")
    private String resource;

    @Column(name = "AUD_ACTION")
    private String action;

    @Column(name = "APPLIC_CD")
    private String applicationCode;

    @Column(name = "AUD_USERAGENT", length = 512)
    private String userAgent;
    
    @Column(name = "AUD_GEOLOCATION")
    private String geoLocation;

    @Column(name = "AUD_DATE", nullable = false)
    private ZonedDateTime recordDate;

    @Column(name = "AUD_LOCALE", nullable = false)
    private Locale locale;

    @Column(name = "AUD_HEADERS", columnDefinition = "longvarchar")
    private String headers;

    @Column(name = "AUD_EXTRA_INFO", columnDefinition = "longvarchar")
    private String extraInfo;

    public AuditTrailEntity() {
        this.id = System.currentTimeMillis();
    }
}
