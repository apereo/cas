package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlAttributeStatementBuilder extends AbstractSaml20ObjectBuilder implements
        SamlProfileObjectBuilder<AttributeStatement> {
    private static final long serialVersionUID = 1815697787562189088L;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private SamlAttributeEncoder samlAttributeEncoder;
    
    @Override
    public AttributeStatement build(final AuthnRequest authnRequest,
                                    final HttpServletRequest request, final HttpServletResponse response,
                                    final Assertion assertion, final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildAttributeStatement(assertion, authnRequest, service, adaptor);
    }

    private AttributeStatement buildAttributeStatement(final Assertion assertion, 
                                                       final AuthnRequest authnRequest,
                                                       final SamlRegisteredService service,
                                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        final Map<String, Object> attributes = new HashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        final Map<String, Object> encodedAttrs = this.samlAttributeEncoder.encode(authnRequest, attributes, service, adaptor);
        return newAttributeStatement(encodedAttrs, casProperties.getAuthn().getSamlIdp().getResponse().isUseAttributeFriendlyName());
    }

    public void setSamlAttributeEncoder(final SamlAttributeEncoder samlAttributeEncoder) {
        this.samlAttributeEncoder = samlAttributeEncoder;
    }
}
