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
 * Implementation of a view to return a SAML SOAP response and assertion, based on
 * the SAML 1.1 specification.
 * <p>
 * If an AttributePrincipal is supplied, then the assertion will include the
 * attributes from it (assuming a String key/Object value pair). The only
 * Authentication attribute it will look at is the authMethod (if supplied).
 * <p>
 * Note that this class will currently not handle proxy authentication.
 * <p>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10SuccessResponseView extends AbstractSaml10ResponseView {

    public Saml10SuccessResponseView(final ProtocolAttributeEncoder protocolAttributeEncoder,
                                     final ServicesManager servicesManager,
                                     final ArgumentExtractor samlArgumentExtractor,
                                     final String encoding,
                                     final AuthenticationAttributeReleasePolicy authAttrReleasePolicy,
                                     final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                     final CasProtocolAttributesRenderer attributesRenderer,
                                     final SamlResponseBuilder samlResponseBuilder) {
        super(true, protocolAttributeEncoder, servicesManager, samlArgumentExtractor,
            encoding, authAttrReleasePolicy, serviceSelectionStrategy, attributesRenderer, samlResponseBuilder);
    }

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {
        val service = getAssertionFrom(model).getService();
        val authentication = getPrimaryAuthenticationFrom(model);
        val principal = getPrincipal(model);
        val registeredService = this.servicesManager.findServiceBy(service);
        val authnAttributes = getCasProtocolAuthenticationAttributes(model, registeredService);
        val principalAttributes = getPrincipalAttributesAsMultiValuedAttributes(model);
        this.samlResponseBuilder.prepareSuccessfulResponse(response, service, authentication,
            principal, authnAttributes, principalAttributes);
    }
}
