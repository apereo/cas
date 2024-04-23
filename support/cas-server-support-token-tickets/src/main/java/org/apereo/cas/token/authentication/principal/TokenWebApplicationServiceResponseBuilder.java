package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.UrlValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class TokenWebApplicationServiceResponseBuilder extends WebApplicationServiceResponseBuilder {
    @Serial
    private static final long serialVersionUID = -2863268279032438778L;

    private final transient TokenTicketBuilder tokenTicketBuilder;

    public TokenWebApplicationServiceResponseBuilder(final ServicesManager servicesManager,
                                                     final TokenTicketBuilder tokenTicketBuilder,
                                                     final UrlValidator urlValidator) {
        super(servicesManager, urlValidator);
        this.tokenTicketBuilder = tokenTicketBuilder;
    }

    @Override
    protected WebApplicationService buildInternal(final WebApplicationService service, final Map<String, String> parameters) {
        val registeredService = servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        val tokenAsResponse = RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.isAssignedTo(registeredService);
        val ticketIdAvailable = isTicketIdAvailable(parameters);

        if (!tokenAsResponse || !ticketIdAvailable) {
            if (ticketIdAvailable) {
                LOGGER.debug("""
                        Registered service [{}] is not configured to issue JWTs for service tickets.
                        Make sure the service property [{}] is defined and set to true
                        """.stripIndent(),
                    registeredService,
                    RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName());
            }
            return super.buildInternal(service, parameters);
        }

        val jwt = generateToken(service, parameters);
        val jwtService = new TokenWebApplicationService(service.getId(), service.getOriginalUrl(), service.getArtifactId());
        jwtService.setFormat(service.getFormat());
        jwtService.setLoggedOutAlready(service.isLoggedOutAlready());

        parameters.put(CasProtocolConstants.PARAMETER_TICKET, jwt);
        parameters.put(Response.ResponseType.REDIRECT.name().toLowerCase(Locale.ENGLISH), Boolean.TRUE.toString());

        return jwtService;
    }

    private static boolean isTicketIdAvailable(final Map<String, String> parameters) {
        return StringUtils.isNotBlank(parameters.get(CasProtocolConstants.PARAMETER_TICKET));
    }

    protected String generateToken(final WebApplicationService service, final Map<String, String> parameters) {
        return FunctionUtils.doUnchecked(() -> {
            val ticketId = parameters.get(CasProtocolConstants.PARAMETER_TICKET);
            return tokenTicketBuilder.build(ticketId, service);
        });
    }
}
