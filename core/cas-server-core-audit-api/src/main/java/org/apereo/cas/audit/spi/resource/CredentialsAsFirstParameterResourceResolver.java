package org.apereo.cas.audit.spi.resource;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.RegisteredServiceAwareAuthenticationTransaction;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager.AuditFormats;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * Converts the Credential object into a String resource identifier.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@RequiredArgsConstructor
public class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {
    protected final AuthenticationServiceSelectionPlan serviceSelectionStrategy;
    protected final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, @Nullable final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    protected String[] toResources(final Object[] args) {
        val object = args[0];
        if (object instanceof final AuthenticationTransaction transaction) {
            return new String[]{tranactionToResourceString(transaction)};
        }
        return new String[]{toResourceString(CollectionUtils.wrap(object))};
    }

    protected String tranactionToResourceString(final AuthenticationTransaction transaction) {
        val payload = CollectionUtils.wrap("credential", transaction.getCredentials());
        if (transaction instanceof final RegisteredServiceAwareAuthenticationTransaction rsat) {
            FunctionUtils.doIfNotNull(rsat.getRegisteredService(), registeredService -> {
                payload.put("registeredServiceUrl", registeredService.getServiceId());
                payload.put("registeredServiceId", registeredService.getId());
                payload.put("registeredServiceFriendlyName", registeredService.getFriendlyName());
                payload.put("registeredServiceName", registeredService.getName());
                payload.put("service", getServiceId(transaction.getService()));
            });
        }
        val auditFormat = AuditFormats.valueOf(properties.getAuditFormat().name());
        return auditFormat.serialize(payload);
    }

    protected String toResourceString(final Object credential) {
        val auditFormat = AuditFormats.valueOf(properties.getAuditFormat().name());
        return auditFormat.serialize(credential);
    }

    private String getServiceId(final Service givenService) throws Throwable {
        val service = Objects.requireNonNull(serviceSelectionStrategy.resolveService(givenService));
        return DigestUtils.abbreviate(service.getId(), properties.getAbbreviationLength());
    }
}
