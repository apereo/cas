package org.apereo.cas.trusted.authentication.api;

import org.apereo.cas.util.DateTimeUtils;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
    private static final int YEARS_TO_KEEP_RECORD_AS_FOREVER = 100;

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
    @Column(name = "recordDate", nullable = false, columnDefinition = "timestamp")
    private ZonedDateTime recordDate = ZonedDateTime.now(ZoneOffset.UTC);

    @JsonProperty("recordKey")
    @Column(name = "recordKey", length = 4_000, nullable = false)
    private String recordKey;

    @JsonProperty("name")
    @Column(name = "recordName", length = 4_000, nullable = false)
    private String name;

    @JsonProperty("expirationDate")
    @Column(name = "expirationDate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

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
        val now = ZonedDateTime.now(ZoneOffset.UTC);
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
     * @return true/false
     */
    @JsonIgnore
    public boolean isExpired() {
        if (this.expirationDate == null) {
            return false;
        }
        val expDate = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        val zonedExpDate = DateTimeUtils.zonedDateTimeOf(this.expirationDate).truncatedTo(ChronoUnit.SECONDS);
        return expDate.equals(zonedExpDate) || expDate.isAfter(zonedExpDate);
    }

    /**
     * Set expiration date of record in given time.
     *
     * @param expiration the expiration
     * @param timeUnit   the time unit
     */
    public void expireIn(final long expiration, final ChronoUnit timeUnit) {
        val expDate = ZonedDateTime.now(ZoneOffset.UTC).plus(expiration, timeUnit).truncatedTo(ChronoUnit.SECONDS);
        val zonedExpDate = DateTimeUtils.dateOf(expDate);
        setExpirationDate(zonedExpDate);
    }

    @Override
    public int compareTo(final MultifactorAuthenticationTrustRecord o) {
        return this.recordDate.compareTo(o.getRecordDate());
    }

    /**
     * Set the expiration date to never expire the record.
     */
    @JsonIgnore
    public void neverExpire() {
        val expDate = getRecordDate().plusYears(YEARS_TO_KEEP_RECORD_AS_FOREVER).truncatedTo(ChronoUnit.SECONDS);
        val zonedExpDate = DateTimeUtils.dateOf(expDate);
        setExpirationDate(zonedExpDate);
    }
}
