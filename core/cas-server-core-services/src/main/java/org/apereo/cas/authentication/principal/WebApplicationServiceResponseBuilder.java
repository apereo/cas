package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Default response builder that passes back the ticket
 * id to the original url of the service based on the response type.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {
    private static final long serialVersionUID = -851233878780818494L;
    private Response.ResponseType responseType;

    /**
     * Instantiates a new Web application service response builder.
     * @param type the type
     */
    public WebApplicationServiceResponseBuilder(final Response.ResponseType type) {
        this.responseType = type;
    }

    @Override
    public Response build(final WebApplicationService service, final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, ticketId);
        }

        if (this.responseType == Response.ResponseType.POST) {
            return buildPost(service, parameters);
        }
        if (this.responseType == Response.ResponseType.REDIRECT) {
            return buildRedirect(service, parameters);
        }

        throw new IllegalArgumentException("Response type is valid. Only POST/REDIRECT are supported");
    }



}
