package org.apereo.cas.trusted.authentication.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class MultifactorAuthenticationTrustRecord implements Comparable<MultifactorAuthenticationTrustRecord> {
    private static final int YEARS_TO_KEEP_RECORD_AS_FOREVER = 1000;

    @Id
    @Transient
    @JsonProperty("id")
    private long id = -1;

    @Column(nullable = false)
    @JsonProperty("principal")
    private String principal;

    @JsonProperty("deviceFingerprint")
    @Column(nullable = false, length = 2048, name = "deviceFingerprint")
    private String deviceFingerprint;

    @JsonProperty("recordDate")
    @Column(name = "recordDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime recordDate = LocalDateTime.now(ZoneOffset.UTC);

    @JsonProperty("recordKey")
    @Column(name = "recordKey", length = 4_000, nullable = false)
    private String recordKey;

    @JsonProperty("name")
    @Column(name = "recordName", length = 4_000, nullable = false)
    private String name;

    @JsonProperty("expirationDate")
    @Column(name = "expirationDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expirationDate;

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
    public static MultifactorAuthenticationTrustRecord newInstance(final String principal,
                                                                   final String geography,
                                                                   final String fingerprint) {
        val r = new MultifactorAuthenticationTrustRecord();
        val now = LocalDateTime.now(ZoneOffset.UTC);
        r.setRecordDate(now.truncatedTo(ChronoUnit.SECONDS));
        r.setPrincipal(principal);
        r.setDeviceFingerprint(fingerprint);
        r.setName(principal.concat("-").concat(now.toString()).concat("-").concat(geography));
        r.neverExpire();
        return r;
    }

    /**
     * Is record expired?
     *
     * @return the boolean
     */
    @JsonIgnore
    public boolean isExpired() {
        if (this.expirationDate == null) {
            return false;
        }
        val expDate = LocalDateTime.now(ZoneOffset.UTC);
        return expDate.isEqual(this.expirationDate) || expDate.isAfter(this.expirationDate);
    }

    /**
     * Set expiration date of record in given time.
     *
     * @param expiration the expiration
     * @param timeUnit   the time unit
     */
    public void expireIn(final long expiration, final ChronoUnit timeUnit) {
        val expDate = LocalDateTime.now(ZoneOffset.UTC).plus(expiration, timeUnit);
        setExpirationDate(expDate);
    }

    @Override
    public int compareTo(final MultifactorAuthenticationTrustRecord o) {
        return this.recordDate.compareTo(o.getRecordDate());
    }

    @JsonIgnore
    public void neverExpire() {
        setExpirationDate(getRecordDate().plusYears(YEARS_TO_KEEP_RECORD_AS_FOREVER));
    }
}
