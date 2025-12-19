package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.spi.AuditActionResolver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import module java.base;

/**
 * Abstract class that encapsulates the required suffixes.
 *
 * @author Scott Battaglia
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSuffixAwareAuditActionResolver implements AuditActionResolver {

    private final String successSuffix;

    private final String failureSuffix;
}
