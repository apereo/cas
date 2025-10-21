package org.apereo.cas.trusted.authentication.api;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.data.annotation.Id;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
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
@NoArgsConstructor
public class MultifactorAuthenticationTrustRecord implements Comparable<MultifactorAuthenticationTrustRecord>, Serializable {
    private static final int YEARS_TO_KEEP_RECORD_AS_FOREVER = 100;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @Serial
    private static final long serialVersionUID = -5263885151448276769L;

    @Id
    @Transient
    @JsonProperty("id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

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

    @JsonProperty("multifactorAuthenticationProvider")
    @Column(name = "multifactorAuthenticationProvider", nullable = true)
    private String multifactorAuthenticationProvider;

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
        val record = new MultifactorAuthenticationTrustRecord();
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        record.setRecordDate(now.truncatedTo(ChronoUnit.SECONDS));
        record.setPrincipal(principal);
        record.setDeviceFingerprint(fingerprint);
        record.setName(principal.concat("-").concat(now.toString()).concat("-").concat(geography));
        record.neverExpire();
        return record;
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

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }
}
