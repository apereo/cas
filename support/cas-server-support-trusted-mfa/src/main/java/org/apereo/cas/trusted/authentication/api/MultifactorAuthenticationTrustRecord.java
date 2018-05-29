package org.apereo.cas.trusted.authentication.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.jpa.SkippingNanoSecondsLocalDateTimeConverter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "MultifactorAuthenticationTrustRecord")
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class MultifactorAuthenticationTrustRecord implements Comparable<MultifactorAuthenticationTrustRecord> {

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(nullable = false)
    private String principal;

    @Column(nullable = false)
    private String deviceFingerprint;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @Convert(converter = SkippingNanoSecondsLocalDateTimeConverter.class)
    private LocalDateTime recordDate;

    @Column(length = 10_000, nullable = false)
    private String recordKey;

    @Column(length = 10_000, nullable = false)
    private String name;

    public MultifactorAuthenticationTrustRecord() {
        this.id = System.currentTimeMillis();
    }

    /**
     * New instance of authentication trust record.
     *
     * @param principal   the principal
     * @param geography   the geography
     * @param fingerprint the device fingerprint
     * @return the authentication trust record
     */
    public static MultifactorAuthenticationTrustRecord newInstance(final String principal, final String geography, final String fingerprint) {
        final var r = new MultifactorAuthenticationTrustRecord();
        r.setRecordDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        r.setPrincipal(principal);
        r.setDeviceFingerprint(fingerprint);
        r.setName(principal.concat("-").concat(LocalDate.now().toString()).concat("-").concat(geography));
        return r;
    }

    @Override
    public int compareTo(final MultifactorAuthenticationTrustRecord o) {
        return this.recordDate.compareTo(o.getRecordDate());
    }
}
