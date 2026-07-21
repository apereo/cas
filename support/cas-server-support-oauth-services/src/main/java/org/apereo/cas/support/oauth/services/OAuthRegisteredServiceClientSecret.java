package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OAuthRegisteredServiceClientSecret}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode
@With
@Slf4j
public class OAuthRegisteredServiceClientSecret implements Serializable {
    @Serial
    private static final long serialVersionUID = 3788828190065822582L;
    
    private String value;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Nullable
    private String expiration;

    public OAuthRegisteredServiceClientSecret(final String value, final long expiration) {
        this(value, String.valueOf(expiration));
    }

    public OAuthRegisteredServiceClientSecret(final String value, final ZonedDateTime expirationDate) {
        this(value, String.valueOf(expirationDate.toEpochSecond()));
    }

    /**
     * Expire at.
     *
     * @param time the time
     * @return the o auth registered service client secret
     */
    @CanIgnoreReturnValue
    public OAuthRegisteredServiceClientSecret expireAt(final ZonedDateTime time) {
        this.expiration = String.valueOf(time.toEpochSecond());
        return this;
    }
    
    /**
     * Is secret without expiration?
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isWithoutExpiration() {
        return StringUtils.isBlank(expiration) && StringUtils.isNotBlank(value);
    }

    /**
     * Is client secret expired?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    public boolean hasClientSecretExpired(final OAuthRegisteredService registeredService) {
        if (StringUtils.isNotBlank(expiration)) {
            val expirationTime = toEffectiveExpiration();
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
            LOGGER.debug("Client secret is set to expire at [{}], while now is [{}]", expirationTime, currentTime);
            if (currentTime.isAfter(expirationTime)) {
                LOGGER.warn("Client secret for service [{}] has expired at [{}] and must be renewed",
                    registeredService.getName(), expirationTime);
                return true;
            }
        }
        return false;
    }

    /**
     * To effective expiration date.
     *
     * @return the zoned date time
     */
    @JsonIgnore
    public ZonedDateTime toEffectiveExpiration() {
        if (NumberUtils.isParsable(expiration)) {
            return DateTimeUtils.zonedDateTimeOf(Instant.ofEpochSecond(Long.parseLong(expiration))).truncatedTo(ChronoUnit.SECONDS);
        }
        if (!expiration.contains(":") && !expiration.contains("T")) {
            val localDate = expiration.contains("/")
                ? DateTimeUtils.localDateOf(expiration)
                : LocalDate.parse(expiration);
            return localDate.atStartOfDay(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        }
        return DateTimeUtils.localDateTimeOf(expiration).atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * A client secret without an expiration.
     *
     * @param value the value
     * @return the secret
     */
    public static OAuthRegisteredServiceClientSecret withoutExpiration(final String value) {
        return new OAuthRegisteredServiceClientSecret(value, (String) null);
    }
}
