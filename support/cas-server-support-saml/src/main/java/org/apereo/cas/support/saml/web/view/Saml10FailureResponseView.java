package org.apereo.cas.support.saml.web.view;


import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;

import java.util.Map;

/**
 * Represents a failed attempt at validating a ticket, responding via a SAML SOAP message.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10FailureResponseView extends AbstractSaml10ResponseView {

    public Saml10FailureResponseView(
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            final ServicesManager servicesManager,
            final String authenticationContextAttribute,
            final Saml10ObjectBuilder samlObjectBuilder,
            final ArgumentExtractor samlArgumentExtractor, 
            final String encoding, 
            final int skewAllowance,
            final int issueLength,
            final AuthenticationAttributeReleasePolicy authAttrReleasePolicy) {
        super(false, protocolAttributeEncoder, servicesManager, authenticationContextAttribute, samlObjectBuilder,
                samlArgumentExtractor, encoding, skewAllowance, issueLength, authAttrReleasePolicy);
    }

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {
        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.REQUEST_DENIED, (String) model.get("description")));
    }
}
