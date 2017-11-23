package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
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
public class WebApplicationServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -851233878780818494L;

    public WebApplicationServiceResponseBuilder() {
    }

    @Override
    public Response build(final WebApplicationService service, final String serviceTicketId,
                          final Authentication authentication) {
        final Map<String, String> parameters = new HashMap<>();
        if (StringUtils.hasText(serviceTicketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, serviceTicketId);
        }

        final WebApplicationService finalService = buildInternal(service, parameters);

        final Response.ResponseType responseType = getWebApplicationServiceResponseType();
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
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public boolean supports(final WebApplicationService service) {
        return true;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
