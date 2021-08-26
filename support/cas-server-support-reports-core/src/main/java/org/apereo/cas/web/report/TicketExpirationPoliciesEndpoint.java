package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.lang.Nullable;

import java.io.Serializable;
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
@Endpoint(id = "ticketExpirationPolicies", enableByDefault = false)
public class TicketExpirationPoliciesEndpoint extends BaseCasActuatorEndpoint {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final List<ExpirationPolicyBuilder> expirationPolicyBuilders;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public TicketExpirationPoliciesEndpoint(final CasConfigurationProperties casProperties,
                                            final List<ExpirationPolicyBuilder> expirationPolicyBuilders,
                                            final ServicesManager servicesManager,
                                            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        super(casProperties);
        this.expirationPolicyBuilders = expirationPolicyBuilders;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    private static String getTicketExpirationPolicyDetails(final Serializable policy) throws Exception {
        return MAPPER.writeValueAsString(policy);
    }

    /**
     * Produce expiration policies.
     *
     * @param serviceId the service
     * @return the map
     * @throws Exception the exception
     */
    @ReadOperation
    @Operation(summary = "Produce expiration policies given an optional service id", parameters = {@Parameter(name = "serviceId")})
    public Map<String, String> handle(@Nullable final String serviceId) throws Exception {
        val model = new HashMap<String, String>();
        expirationPolicyBuilders.forEach(Unchecked.consumer(builder -> {
            val policy = builder.buildTicketExpirationPolicy();
            val details = getTicketExpirationPolicyDetails(policy);
            model.put(builder.getTicketType().getName(), details);
        }));

        val registeredService = StringUtils.isNotBlank(serviceId)
            ? NumberUtils.isCreatable(serviceId)
                ? servicesManager.findServiceBy(Long.parseLong(serviceId))
                : servicesManager.findServiceBy(webApplicationServiceFactory.createService(serviceId))
            : null;

        Optional.ofNullable(registeredService)
            .map(RegisteredService::getTicketGrantingTicketExpirationPolicy)
            .map(RegisteredServiceTicketGrantingTicketExpirationPolicy::toExpirationPolicy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .ifPresent(Unchecked.consumer(policy -> {
                val details = getTicketExpirationPolicyDetails(policy);
                model.put(TicketGrantingTicket.class.getName().concat(registeredService.getName()), details);
            }));

        Optional.ofNullable(registeredService)
            .map(RegisteredService::getServiceTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> {
                val details = getTicketExpirationPolicyDetails(policy);
                model.put(ServiceTicket.class.getName().concat(registeredService.getName()), details);
            }));

        Optional.ofNullable(registeredService)
            .map(RegisteredService::getProxyGrantingTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> {
                val details = getTicketExpirationPolicyDetails(policy);
                model.put(ProxyGrantingTicket.class.getName().concat(registeredService.getName()), details);
            }));

        Optional.ofNullable(registeredService)
            .map(RegisteredService::getProxyTicketExpirationPolicy)
            .ifPresent(Unchecked.consumer(policy -> {
                val details = getTicketExpirationPolicyDetails(policy);
                model.put(ProxyTicket.class.getName().concat(registeredService.getName()), details);
            }));

        return model;
    }
}
