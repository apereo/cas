package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlAttributeStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AttributeStatement> {
    private static final long serialVersionUID = 1815697787562189088L;
    private final transient ProtocolAttributeEncoder samlAttributeEncoder;
    private final SamlIdPProperties samlIdPProperties;
    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final ProtocolAttributeEncoder samlAttributeEncoder,
                                                    final SamlIdPProperties samlIdPProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter) {
        super(configBean);
        this.samlAttributeEncoder = samlAttributeEncoder;
        this.samlIdPProperties = samlIdPProperties;
        this.samlObjectEncrypter = samlObjectEncrypter;
    }

    @Override
    public AttributeStatement build(final RequestAbstractType authnRequest,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final Object assertion,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String binding,
                                    final MessageContext messageContext) throws SamlException {
        return buildAttributeStatement(assertion, authnRequest, service, adaptor, messageContext);
    }

    private AttributeStatement buildAttributeStatement(final Object casAssertion,
                                                       final RequestAbstractType authnRequest,
                                                       final SamlRegisteredService service,
                                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                       final MessageContext messageContext) throws SamlException {

        val assertion = Assertion.class.cast(casAssertion);
        val attributes = new HashMap<String, Object>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        val encodedAttrs = this.samlAttributeEncoder.encodeAttributes(attributes, service);

        val resp = samlIdPProperties.getResponse();
        val nameFormats = new HashMap<String, String>(resp.configureAttributeNameFormats());
        nameFormats.putAll(service.getAttributeNameFormats());

        val globalFriendlyNames = samlIdPProperties.getAttributeFriendlyNames();
        val friendlyNames = new HashMap<String, String>(CollectionUtils.convertDirectedListToMap(globalFriendlyNames));
        friendlyNames.putAll(service.getAttributeFriendlyNames());

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(service, adaptor, messageContext, samlObjectEncrypter);
        return newAttributeStatement(encodedAttrs, friendlyNames,
            service.getAttributeValueTypes(),
            nameFormats,
            resp.getDefaultAttributeNameFormat(),
            attrBuilder);
    }
}
