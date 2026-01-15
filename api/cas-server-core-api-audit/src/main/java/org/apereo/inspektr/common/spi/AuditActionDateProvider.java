package org.apereo.inspektr.common.spi;

import module java.base;

/**
 * This is {@link AuditActionDateProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface AuditActionDateProvider extends Supplier<LocalDateTime> {

    /**
     * Utc audit action date provider.
     *
     * @return the audit action date provider
     */
    static AuditActionDateProvider utc() {
        return () -> LocalDateTime.now(Clock.systemUTC());
    }

    /**
     * System audit action date provider.
     *
     * @return the audit action date provider
     */
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    static AuditActionDateProvider system() {
        return () -> LocalDateTime.now(Clock.systemDefaultZone());
    }
}
