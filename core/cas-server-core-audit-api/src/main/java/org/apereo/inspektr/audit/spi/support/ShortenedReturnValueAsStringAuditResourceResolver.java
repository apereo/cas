package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import java.util.Arrays;

/**
 * This is {@link ShortenedReturnValueAsStringAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
public class ShortenedReturnValueAsStringAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    private static final int MAX_WIDTH = 125;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        val resources = super.resolveFrom(auditableTarget, retval);
        if (auditFormat == AuditTrailManager.AuditFormats.JSON) {
            return resources;
        }
        if (resources != null) {
            return Arrays.stream(resources)
                .map(r -> StringUtils.abbreviate(r, MAX_WIDTH))
                .toList()
                .toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }
        return null;
    }
}

