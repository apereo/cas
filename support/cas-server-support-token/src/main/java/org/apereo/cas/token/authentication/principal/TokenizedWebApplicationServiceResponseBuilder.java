package org.apereo.cas.token.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenConstants;

import java.util.Map;

/**
 * This is {@link TokenizedWebApplicationServiceResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TokenizedWebApplicationServiceResponseBuilder extends WebApplicationServiceResponseBuilder {
    private static final long serialVersionUID = -2863268279032438778L;

    private transient ServicesManager servicesManager;

    public TokenizedWebApplicationServiceResponseBuilder(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    protected WebApplicationService buildInternal(final WebApplicationService service,
                                                  final Map<String, String> parameters) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        final Map.Entry<String, RegisteredServiceProperty> property = registeredService.getProperties()
                .entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(TokenConstants.PROPERTY_NAME_TOKEN_AS_RESPONSE))
                .distinct()
                .findFirst()
                .orElse(null);

        if (property == null) {
            return super.buildInternal(service, parameters);
        }

        return service;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
