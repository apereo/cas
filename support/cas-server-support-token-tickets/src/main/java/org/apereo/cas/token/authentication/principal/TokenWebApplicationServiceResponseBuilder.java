package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class TokenWebApplicationServiceResponseBuilder extends WebApplicationServiceResponseBuilder {
    private static final long serialVersionUID = -2863268279032438778L;

    private final transient TokenTicketBuilder tokenTicketBuilder;

    public TokenWebApplicationServiceResponseBuilder(final ServicesManager servicesManager,
                                                     final TokenTicketBuilder tokenTicketBuilder) {
        super(servicesManager);
        this.tokenTicketBuilder = tokenTicketBuilder;
    }

    @Override
    protected WebApplicationService buildInternal(final WebApplicationService service, final Map<String, String> parameters) {
        val registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        val tokenAsResponse = RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.isAssignedTo(registeredService);

        if (!tokenAsResponse) {
            LOGGER.debug("Registered service [{}] is not configured to issue JWTs for service tickets. "
                    + "Make sure the service property [{}] is defined and is set to true", registeredService,
                RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName());
            return super.buildInternal(service, parameters);
        }

        val jwt = generateToken(service, parameters);
        val jwtService = new TokenWebApplicationService(service.getId(), service.getOriginalUrl(), service.getArtifactId());
        jwtService.setFormat(service.getFormat());
        jwtService.setLoggedOutAlready(service.isLoggedOutAlready());

        parameters.put(CasProtocolConstants.PARAMETER_TICKET, jwt);
        parameters.put(Response.ResponseType.REDIRECT.name().toLowerCase(), Boolean.TRUE.toString());

        return jwtService;
    }

    /**
     * Generate token string.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the jwt
     */
    @SneakyThrows
    protected String generateToken(final Service service, final Map<String, String> parameters) {
        val ticketId = parameters.get(CasProtocolConstants.PARAMETER_TICKET);
        return this.tokenTicketBuilder.build(ticketId, service);
    }
}
