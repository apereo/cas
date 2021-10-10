package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
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
import java.util.List;
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

    private final SamlIdPProperties samlIdPProperties;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    private final AttributeDefinitionStore attributeDefinitionStore;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final SamlIdPProperties samlIdPProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter,
                                                    final AttributeDefinitionStore attributeDefinitionStore,
                                                    final ServiceFactory<WebApplicationService> serviceFactory) {
        super(configBean);
        this.samlIdPProperties = samlIdPProperties;
        this.samlObjectEncrypter = samlObjectEncrypter;
        this.attributeDefinitionStore = attributeDefinitionStore;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public AttributeStatement build(final RequestAbstractType authnRequest,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final Object casAssertion,
                                    final SamlRegisteredService registeredService,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String binding,
                                    final MessageContext messageContext) throws SamlException {
        val assertion = Assertion.class.cast(casAssertion);
        val attributes = new HashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        val webApplicationService = serviceFactory.createService(adaptor.getEntityId(), WebApplicationService.class);
        val encodedAttrs = ProtocolAttributeEncoder.decodeAttributes(attributes, registeredService, webApplicationService);

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(registeredService, adaptor, samlObjectEncrypter);
        return newAttributeStatement(encodedAttrs, attrBuilder, registeredService);
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
        val attrStatement = SamlUtils.newSamlObject(AttributeStatement.class);

        val resp = samlIdPProperties.getResponse();
        val nameFormats = new HashMap<>(resp.configureAttributeNameFormats());
        nameFormats.putAll(samlRegisteredService.getAttributeNameFormats());

        val globalFriendlyNames = samlIdPProperties.getCore().getAttributeFriendlyNames();
        val friendlyNames = new HashMap<>(CollectionUtils.convertDirectedListToMap(globalFriendlyNames));
        val urns = new HashMap<String, String>();

        attributeDefinitionStore.getAttributeDefinitions()
            .stream()
            .filter(defn -> defn instanceof SamlIdPAttributeDefinition)
            .map(SamlIdPAttributeDefinition.class::cast)
            .forEach(defn -> {
                if (StringUtils.isNotBlank(defn.getFriendlyName())) {
                    friendlyNames.put(defn.getKey(), defn.getFriendlyName());
                }
                if (StringUtils.isNotBlank(defn.getUrn())) {
                    urns.put(defn.getKey(), defn.getUrn());
                }
            });

        friendlyNames.putAll(samlRegisteredService.getAttributeFriendlyNames());

        SamlIdPAttributeDefinitionCatalog.load()
            .filter(defn -> !friendlyNames.containsKey(defn.getKey()))
            .forEach(defn -> {
                friendlyNames.put(defn.getKey(), defn.getFriendlyName());
                urns.put(defn.getKey(), defn.getUrn());
            });

        for (val entry : attributes.entrySet()) {
            if (entry.getValue() instanceof Collection<?> && ((Collection<?>) entry.getValue()).isEmpty()) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", entry.getKey());
                continue;
            }
            val friendlyName = friendlyNames.getOrDefault(entry.getKey(), null);

            val attributeNames = urns.containsKey(entry.getKey())
                ? List.of(urns.get(entry.getKey()))
                : getMappedAttributeNamesFromAttributeDefinitionStore(entry);

            attributeNames.forEach(name -> {
                LOGGER.trace("Creating SAML attribute [{}] with value [{}], friendlyName [{}]", attributeNames, entry.getValue(), friendlyName);
                val attribute = newAttribute(friendlyName, name, entry.getValue(),
                    nameFormats,
                    resp.getDefaultAttributeNameFormat(),
                    samlRegisteredService.getAttributeValueTypes());

                LOGGER.trace("Created SAML attribute [{}] with nameid-format [{}]", attribute.getName(), attribute.getNameFormat());
                builder.build(attrStatement, attribute);
            });
        }

        return attrStatement;
    }

    /**
     * Gets mapped attribute names from attribute definition store.
     *
     * @param entry the entry
     * @return the mapped attribute names from attribute definition store
     */
    protected Collection<String> getMappedAttributeNamesFromAttributeDefinitionStore(final Map.Entry<String, Object> entry) {
        return org.springframework.util.StringUtils.commaDelimitedListToSet(
            attributeDefinitionStore.locateAttributeDefinition(entry.getKey())
                .map(AttributeDefinition::getName)
                .filter(StringUtils::isNotBlank)
                .orElseGet(entry::getKey));
    }
}
