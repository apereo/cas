package org.apereo.inspektr.audit.spi.support;

import module java.base;
import org.apereo.inspektr.audit.AuditTrailManager;

/**
 * Returns the parameters as an array of strings.
 *
 * @author Scott Battaglia
 * @since 1.0.0
 */
public class ParametersAsStringResourceResolver extends AbstractAuditResourceResolver {

    @Override
    protected String[] createResource(final Object[] args) {
        return Arrays.stream(args).map(this::toResourceString).toArray(String[]::new);
    }

    /**
     * To resource string.
     *
     * @param arg the arg
     * @return the string
     */
    public String toResourceString(final Object arg) {
        if (auditFormat == AuditTrailManager.AuditFormats.JSON) {
            return AuditTrailManager.toJson(arg);
        }
        return arg.toString();
    }
}
