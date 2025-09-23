package org.apereo.cas.web.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.val;
import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * This is {@link ThrottledSubmission}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Data
@SuperBuilder
@AllArgsConstructor
@With
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor(force = true)
public class ThrottledSubmission implements Serializable {
    @Serial
    private static final long serialVersionUID = -853401483455717926L;

    private final String id;

    private final String key;

    @Builder.Default
    private final ZonedDateTime value = ZonedDateTime.now(Clock.systemUTC());

    private final String username;

    private final String clientIpAddress;

    private ZonedDateTime expiration;

    /**
     * Compares the current time with the expiration time to determine
     * whether the submission has expired.
     *
     * @return true or false
     */
    public boolean hasExpiredAlready() {
        val now = ZonedDateTime.now(Clock.systemUTC());
        return expiration == null || now.isAfter(expiration) || now.isEqual(expiration);
    }

    /**
     * Is the entry still locked and in its expiration window?
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isStillInExpirationWindow() {
        val now = ZonedDateTime.now(Clock.systemUTC());
        return expiration != null && (expiration.isAfter(now) || expiration.isEqual(now));
    }
}
