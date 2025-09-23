package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantAuthenticationHandlerBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface TenantAuthenticationHandlerBuilder {
    /**
     * Build authentication.
     *
     * @param tenantDefinition the tenant definition
     * @return the authentication
     */
    default List<? extends AuthenticationHandler> build(final TenantDefinition tenantDefinition) {
        if (!tenantDefinition.getProperties().isEmpty()) {
            val bindingContext = tenantDefinition.bindProperties();
            return buildInternal(tenantDefinition, bindingContext);
        }
        return List.of();
    }

    /**
     * Build internal list of handlers.
     *
     * @param tenantDefinition the tenant definition
     * @param bindingContext   the cas properties
     * @return the list
     */
    List<? extends AuthenticationHandler> buildInternal(TenantDefinition tenantDefinition,
                                                        ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext);
}
