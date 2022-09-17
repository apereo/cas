package org.apereo.cas.web.support;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

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
public class ThrottledSubmission implements Serializable {
    @Serial
    private static final long serialVersionUID = -853401483455717926L;

    private final String key;

    @Builder.Default
    private final ZonedDateTime value = ZonedDateTime.now(Clock.systemUTC());

    private final String username;

    private final String clientIpAddress;

    private final ZonedDateTime expiration;
}
