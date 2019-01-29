package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.val;
import org.opensaml.saml.saml1.core.Response;

import java.util.Map;

/**
 * Represents a failed attempt at validating a ticket, responding via a SAML SOAP message.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10FailureResponseView extends AbstractSaml10ResponseView {


    public Saml10FailureResponseView(final ProtocolAttributeEncoder protocolAttributeEncoder,
                                     final ServicesManager servicesManager, final ArgumentExtractor samlArgumentExtractor,
                                     final String encoding, final AuthenticationAttributeReleasePolicy authAttrReleasePolicy,
                                     final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                     final CasProtocolAttributesRenderer attributesRenderer,
                                     final SamlResponseBuilder samlResponseBuilder) {
        super(false, protocolAttributeEncoder, servicesManager, samlArgumentExtractor, encoding,
            authAttrReleasePolicy, serviceSelectionStrategy, attributesRenderer, samlResponseBuilder);
    }

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {
        val description = (String) model.get("description");
        samlResponseBuilder.setStatusRequestDenied(response, description);
    }
}
