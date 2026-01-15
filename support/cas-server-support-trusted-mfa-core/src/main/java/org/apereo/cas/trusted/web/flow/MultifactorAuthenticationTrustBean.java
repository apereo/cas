package org.apereo.cas.trusted.web.flow;

import module java.base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    @Serial
    private static final long serialVersionUID = -4004117228828573006L;

    private String deviceName;

    private long expiration;

    private ChronoUnit timeUnit = ChronoUnit.FOREVER;
}
