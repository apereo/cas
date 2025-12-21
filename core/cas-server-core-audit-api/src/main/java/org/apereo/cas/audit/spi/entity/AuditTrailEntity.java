package org.apereo.cas.audit.spi.entity;

import module java.base;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

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
@NoArgsConstructor
@SuppressWarnings("NullAway.Init")
public class AuditTrailEntity {

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "AUD_USER", length = 512)
    private String user;

    @Column(name = "AUD_CLIENT_IP")
    private String clientIp;

    @Column(name = "AUD_SERVER_IP")
    private String serverIp;

    @Column(name = "AUD_RESOURCE", length = 512)
    private String resource;

    @Column(name = "AUD_ACTION")
    private String action;

    @Column(name = "AUD_TENANT", length = 512)
    private String tenant;

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
}
