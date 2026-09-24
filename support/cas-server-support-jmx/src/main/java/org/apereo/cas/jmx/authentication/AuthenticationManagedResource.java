package org.apereo.cas.jmx.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

/**
 * This is {@link AuthenticationManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ManagedResource(description = "Authentication handler and multifactor provider diagnostics")
@RequiredArgsConstructor
public class AuthenticationManagedResource {
    private final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    private final ObjectProvider<ServicesManager> servicesManager;

    private final ApplicationContext applicationContext;

    @ManagedOperation(description = "List registered authentication handlers as name:order")
    public String[] getAuthenticationHandlers() {
        return authenticationEventExecutionPlan.getObject().getAuthenticationHandlers().stream()
            .map(handler -> String.format("%s:%s", handler.getName(), handler.getOrder()))
            .sorted()
            .toArray(String[]::new);
    }

    @ManagedOperation(description = "List MFA providers as id:friendlyName:order:failureMode without probing availability")
    public String[] getMultifactorAuthenticationProviders() {
        return MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext)
            .values().stream()
            .map(provider -> String.format("%s:%s:%s:%s", provider.getId(), provider.getFriendlyName(),
                provider.getOrder(), provider.getFailureMode()))
            .sorted()
            .toArray(String[]::new);
    }

    /**
     * Check the availability of a specific provider for a registered service.
     *
     * @param providerId the exact provider identifier
     * @param serviceId the registered service numeric identifier
     * @return whether the provider is available
     */
    @ManagedOperation(description = "Check MFA provider availability for a registered service; may contact the provider")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "providerId", description = "Exact MFA provider identifier"),
        @ManagedOperationParameter(name = "serviceId", description = "Registered service numeric identifier")
    })
    public boolean isMultifactorAuthenticationProviderAvailable(final String providerId, final long serviceId) {
        Assert.hasText(providerId, "Provider identifier cannot be blank");
        val service = Optional.ofNullable(servicesManager.getObject().findServiceBy(serviceId))
            .orElseThrow(() -> new IllegalArgumentException("Unknown registered service identifier"));
        val provider = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext)
            .values().stream()
            .filter(candidate -> providerId.equals(candidate.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown multifactor authentication provider identifier"));
        return provider.isAvailable(service);
    }
}
