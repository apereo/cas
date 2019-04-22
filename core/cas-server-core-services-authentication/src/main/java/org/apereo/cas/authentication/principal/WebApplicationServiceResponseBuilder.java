package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Response.ResponseType;
import org.apereo.cas.services.ServicesManager;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Default response builder that passes back the ticket
 * id to the original url of the service based on the response type.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class WebApplicationServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -851233878780818494L;

    private int order = Integer.MAX_VALUE;

    public WebApplicationServiceResponseBuilder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    public Response build(final WebApplicationService service, final String serviceTicketId, final Authentication authentication) {
        val parameters = new HashMap<String, String>();
        if (StringUtils.hasText(serviceTicketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, serviceTicketId);
        }

        val finalService = buildInternal(service, parameters);
        val responseType = getWebApplicationServiceResponseType(finalService);
        if (responseType == ResponseType.POST) {
            return buildPost(finalService, parameters);
        }
        if (responseType == ResponseType.REDIRECT) {
            return buildRedirect(finalService, parameters);
        }
        if (responseType == ResponseType.HEADER) {
            return buildHeader(finalService, parameters);
        }

        throw new IllegalArgumentException("Response type is valid. Only " + Arrays.toString(ResponseType.values()) + " are supported");
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
}
