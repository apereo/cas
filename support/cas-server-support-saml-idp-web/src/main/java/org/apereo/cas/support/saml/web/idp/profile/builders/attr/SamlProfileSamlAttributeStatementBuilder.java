package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.util.Saml20AttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlAttributeStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AttributeStatement> {
    private static final long serialVersionUID = 1815697787562189088L;

    private final transient ProtocolAttributeEncoder samlAttributeEncoder;

    private final SamlIdPProperties samlIdPProperties;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    private final AttributeDefinitionStore attributeDefinitionStore;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final ProtocolAttributeEncoder samlAttributeEncoder,
                                                    final SamlIdPProperties samlIdPProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter,
                                                    final AttributeDefinitionStore attributeDefinitionStore) {
        super(configBean);
        this.samlAttributeEncoder = samlAttributeEncoder;
        this.samlIdPProperties = samlIdPProperties;
        this.samlObjectEncrypter = samlObjectEncrypter;
        this.attributeDefinitionStore = attributeDefinitionStore;
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
        return buildAttributeStatement(assertion, service, adaptor);
    }

    private AttributeStatement buildAttributeStatement(final Object casAssertion,
                                                       final SamlRegisteredService service,
                                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
        throws SamlException {

        val assertion = Assertion.class.cast(casAssertion);
        val attributes = new HashMap<String, Object>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        val encodedAttrs = this.samlAttributeEncoder.encodeAttributes(attributes, service);

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(service, adaptor, samlObjectEncrypter);
        return newAttributeStatement(encodedAttrs, attrBuilder, service);
    }

    /**
     * New attribute statement.
     *
     * @param attributes            the attributes
     * @param builder               the builder
     * @param samlRegisteredService the saml registered service
     * @return the attribute statement
     */
    public AttributeStatement newAttributeStatement(final Map<String, Object> attributes,
                                                    final Saml20AttributeBuilder builder,
                                                    final SamlRegisteredService samlRegisteredService) {
        val attrStatement = newSamlObject(AttributeStatement.class);

        val resp = samlIdPProperties.getResponse();
        val nameFormats = new HashMap<String, String>(resp.configureAttributeNameFormats());
        nameFormats.putAll(samlRegisteredService.getAttributeNameFormats());

        val globalFriendlyNames = samlIdPProperties.getAttributeFriendlyNames();
        val friendlyNames = new HashMap<String, String>(CollectionUtils.convertDirectedListToMap(globalFriendlyNames));

        attributeDefinitionStore.getAttributeDefinitions()
            .stream()
            .filter(defn -> defn instanceof SamlIdPAttributeDefinition)
            .map(SamlIdPAttributeDefinition.class::cast)
            .filter(defn -> StringUtils.isNotBlank(defn.getFriendlyName()))
            .forEach(defn -> friendlyNames.put(defn.getKey(), defn.getFriendlyName()));

        friendlyNames.putAll(samlRegisteredService.getAttributeFriendlyNames());

        for (val e : attributes.entrySet()) {
            if (e.getValue() instanceof Collection<?> && ((Collection<?>) e.getValue()).isEmpty()) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", e.getKey());
                continue;
            }
            val friendlyName = friendlyNames.getOrDefault(e.getKey(), null);

            val name = attributeDefinitionStore.locateAttributeDefinition(e.getKey())
                .map(AttributeDefinition::getName)
                .filter(StringUtils::isNotBlank)
                .orElse(e.getKey());

            LOGGER.trace("Creating SAML attribute [{}] with value [{}], friendlyName [{}]", name, e.getValue(), friendlyName);
            val attribute = newAttribute(friendlyName, name, e.getValue(),
                nameFormats,
                resp.getDefaultAttributeNameFormat(),
                samlRegisteredService.getAttributeValueTypes());

            LOGGER.trace("Created SAML attribute [{}] with nameid-format [{}]", attribute.getName(), attribute.getNameFormat());
            builder.build(attrStatement, attribute);
        }

        return attrStatement;
    }
}
