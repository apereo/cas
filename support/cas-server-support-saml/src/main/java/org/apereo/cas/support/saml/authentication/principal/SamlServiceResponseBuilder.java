package org.apereo.cas.support.saml.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.SamlProtocolConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds responses to SAML service requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4584738964007702003L;

    @Override
    public Response build(final WebApplicationService service, final String ticketId, final Authentication authentication) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(SamlProtocolConstants.CONST_PARAM_ARTIFACT, ticketId);
        return buildRedirect(service, parameters);
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
        return service instanceof SamlService;
    }
}

