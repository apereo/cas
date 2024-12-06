package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link TicketExpirationPoliciesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Endpoint(id = "ticketExpirationPolicies", defaultAccess = Access.NONE)
public class TicketExpirationPoliciesEndpoint extends BaseCasActuatorEndpoint {
    private final List<ExpirationPolicyBuilder> expirationPolicyBuilders;

    private final ObjectProvider<ServicesManager> servicesManagerProvider;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public TicketExpirationPoliciesEndpoint(final CasConfigurationProperties casProperties,
                                            final List<ExpirationPolicyBuilder> expirationPolicyBuilders,
                                            final ObjectProvider<ServicesManager> servicesManager,
                                            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        super(casProperties);
        this.expirationPolicyBuilders = expirationPolicyBuilders;
        this.servicesManagerProvider = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }
    

    /**
     * Produce expiration policies.
     *
     * @param serviceId the service
     * @return the map
     */
    @ReadOperation
    @Operation(summary = "Produce expiration policies given an optional service id",
        parameters = @Parameter(name = "serviceId", required = false, description = "The service id to look up"))
    public Map<String, ?> handle(@Nullable final String serviceId) {
        val model = new HashMap<String, Object>();
        expirationPolicyBuilders.forEach(Unchecked.consumer(builder -> {
            val policy = builder.buildTicketExpirationPolicy();
            model.put(builder.getClass().getSimpleName(), policy);
        }));

        val servicesManager = servicesManagerProvider.getObject();
        val registeredService = StringUtils.isNotBlank(serviceId)
            ? NumberUtils.isCreatable(serviceId)
            ? servicesManager.findServiceBy(Long.parseLong(serviceId))
            : servicesManager.findServiceBy(webApplicationServiceFactory.createService(serviceId))
            : null;

        Optional.ofNullable(registeredService)
            .map(CasModelRegisteredService.class::cast)
            .map(RegisteredService::getTicketGrantingTicketExpirationPolicy)
            .map(RegisteredServiceTicketGrantingTicketExpirationPolicy::toExpirationPolicy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .ifPresent(Unchecked.consumer(policy -> model.put(TicketGrantingTicket.class.getName().concat(registeredService.getName()), policy)));

        Optional.ofNullable(registeredService)
            .map(CasModelRegisteredService.class::cast)
            .map(CasModelRegisteredService::getServiceTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> model.put(ServiceTicket.class.getName().concat(registeredService.getName()), policy)));

        Optional.ofNullable(registeredService)
            .map(CasModelRegisteredService.class::cast)
            .map(CasModelRegisteredService::getProxyGrantingTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> model.put(ProxyGrantingTicket.class.getName().concat(registeredService.getName()), policy)));

        Optional.ofNullable(registeredService)
            .map(CasModelRegisteredService.class::cast)
            .map(CasModelRegisteredService::getProxyTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> model.put(ProxyTicket.class.getName().concat(registeredService.getName()), policy)));

        return model;
    }
}
