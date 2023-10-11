package org.apereo.cas.logout.slo;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SingleLogoutContinuation}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public record SingleLogoutContinuation(String content) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1350244643948535816L;
}
