package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.RequestAbstractType;
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
public class SamlProfileSamlAttributeStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AttributeStatement> {
    private static final long serialVersionUID = 1815697787562189088L;

    @Autowired
    private CasConfigurationProperties casProperties;

    private final ProtocolAttributeEncoder samlAttributeEncoder;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean, final ProtocolAttributeEncoder samlAttributeEncoder) {
        super(configBean);
        this.samlAttributeEncoder = samlAttributeEncoder;
    }

    @Override
    public AttributeStatement build(final RequestAbstractType authnRequest,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final Object assertion,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String binding) throws SamlException {
        return buildAttributeStatement(assertion, authnRequest, service, adaptor);
    }

    private AttributeStatement buildAttributeStatement(final Object casAssertion,
                                                       final RequestAbstractType authnRequest,
                                                       final SamlRegisteredService service,
                                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        
        final Assertion assertion = Assertion.class.cast(casAssertion);
        final Map<String, Object> attributes = new HashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        final Map<String, Object> encodedAttrs = this.samlAttributeEncoder.encodeAttributes(attributes, service);
        
        final SamlIdPProperties.Response resp = casProperties.getAuthn().getSamlIdp().getResponse();
        final Map<String, String> nameFormats = new HashMap<>(resp.configureAttributeNameFormats());
        nameFormats.putAll(service.getAttributeNameFormats());
        return newAttributeStatement(encodedAttrs, resp.isUseAttributeFriendlyName(), nameFormats,
                casProperties.getAuthn().getSamlIdp().getResponse().getDefaultAttributeNameFormat());
    }
}
