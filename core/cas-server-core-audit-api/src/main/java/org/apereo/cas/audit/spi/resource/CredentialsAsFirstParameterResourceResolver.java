package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Converts the Credential object into a String resource identifier.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@RequiredArgsConstructor
public class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {
    private final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    private String[] toResources(final Object[] args) {
        val object = args[0];
        if (object instanceof AuthenticationTransaction transaction) {
            return new String[]{toResourceString(transaction.getCredentials())};
        }
        return new String[]{toResourceString(CollectionUtils.wrap(object))};
    }

    private String toResourceString(final Object credential) {
        val auditFormat = AuditTrailManager.AuditFormats.valueOf(properties.getAuditFormat().name());
        return auditFormat.serialize(credential);
    }
}
