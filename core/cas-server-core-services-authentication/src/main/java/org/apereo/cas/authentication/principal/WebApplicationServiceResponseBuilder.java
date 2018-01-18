package org.apereo.cas.authentication.principal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.ServicesManager;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Default response builder that passes back the ticket
 * id to the original url of the service based on the response type.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Getter
public class WebApplicationServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -851233878780818494L;

    private int order = Integer.MAX_VALUE;

    public WebApplicationServiceResponseBuilder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    public Response build(final WebApplicationService service, final String serviceTicketId, final Authentication authentication) {
        final Map<String, String> parameters = new HashMap<>();
        if (StringUtils.hasText(serviceTicketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, serviceTicketId);
        }

        final WebApplicationService finalService = buildInternal(service, parameters);

        final Response.ResponseType responseType = getWebApplicationServiceResponseType(finalService);
        if (responseType == Response.ResponseType.POST) {
            return buildPost(finalService, parameters);
        }
        if (responseType == Response.ResponseType.REDIRECT) {
            return buildRedirect(finalService, parameters);
        }
        if (responseType == Response.ResponseType.HEADER) {
            return buildHeader(finalService, parameters);
        }

        throw new IllegalArgumentException("Response type is valid. Only POST/REDIRECT are supported");
    }

    /**
     * Build internal service.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the service
     */
    protected WebApplicationService buildInternal(final WebApplicationService service, final Map<String, String> parameters) {
        return service;
    }

    @Override
    public boolean supports(final WebApplicationService service) {
        return true;
    }
}
