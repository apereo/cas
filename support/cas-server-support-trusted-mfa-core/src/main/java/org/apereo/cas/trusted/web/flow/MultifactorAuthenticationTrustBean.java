package org.apereo.cas.trusted.web.flow;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthenticationTrustBean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class MultifactorAuthenticationTrustBean implements Serializable {
    private static final long serialVersionUID = -4004117228828573006L;

    private String deviceName;

    private long expiration;

    private ChronoUnit timeUnit = ChronoUnit.FOREVER;
}
