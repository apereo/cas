package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.util.Saml20AttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
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

    private final SamlProfileObjectBuilder<NameID> samlNameIdBuilder;

    private final MetadataResolver samlIdPMetadataResolver;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final SamlIdPProperties samlIdPProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter,
                                                    final AttributeDefinitionStore attributeDefinitionStore,
                                                    final ServiceFactory<WebApplicationService> serviceFactory,
                                                    final SamlProfileObjectBuilder<NameID> samlNameIdBuilder,
                                                    final MetadataResolver samlIdPMetadataResolver) {
        super(configBean);
        this.samlIdPProperties = samlIdPProperties;
        this.samlObjectEncrypter = samlObjectEncrypter;
        this.attributeDefinitionStore = attributeDefinitionStore;
        this.serviceFactory = serviceFactory;
        this.samlNameIdBuilder = samlNameIdBuilder;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
    }

    @Override
    public AttributeStatement build(final RequestAbstractType authnRequest,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final AuthenticatedAssertionContext assertion,
                                    final SamlRegisteredService registeredService,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String binding,
                                    final MessageContext messageContext) throws SamlException {

        val attributes = new HashMap<>(assertion.getAttributes());

        val webApplicationService = serviceFactory.createService(adaptor.getEntityId(), WebApplicationService.class);
        val encodedAttrs = ProtocolAttributeEncoder.decodeAttributes(attributes, registeredService, webApplicationService);

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(registeredService, adaptor, samlObjectEncrypter);
        return newAttributeStatement(authnRequest, request, response,
            assertion, registeredService, adaptor, binding,
            messageContext, encodedAttrs, attrBuilder);
    }

    /**
     * New attribute statement.
     *
     * @param authnRequest          the authn request
     * @param request               the request
     * @param response              the response
     * @param casAssertion          the cas assertion
     * @param samlRegisteredService the saml registered service
     * @param adaptor               the adaptor
     * @param binding               the binding
     * @param messageContext        the message context
     * @param attributes            the attributes
     * @param builder               the builder
     * @return the attribute statement
     */
    public AttributeStatement newAttributeStatement(final RequestAbstractType authnRequest,
                                                    final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final AuthenticatedAssertionContext casAssertion,
                                                    final SamlRegisteredService samlRegisteredService,
                                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                    final String binding,
                                                    final MessageContext messageContext,
                                                    final Map<String, Object> attributes,
                                                    final Saml20AttributeBuilder builder) {
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
            var attributeValue = entry.getValue();
            if (attributeValue instanceof Collection<?> && ((Collection<?>) attributeValue).isEmpty()) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", entry.getKey());
                continue;
            }
            val friendlyName = friendlyNames.getOrDefault(entry.getKey(), null);

            val attributeNames = urns.containsKey(entry.getKey())
                ? List.of(urns.get(entry.getKey()))
                : getMappedAttributeNamesFromAttributeDefinitionStore(entry);

            for (val name : attributeNames) {
                LOGGER.trace("Processing SAML attribute [{}] with value [{}], friendlyName [{}]", name, attributeValue, friendlyName);
                val valueType = samlRegisteredService.getAttributeValueTypes().get(name);

                if (NameIDType.class.getSimpleName().equalsIgnoreCase(valueType)) {
                    val nameId = samlNameIdBuilder.build(authnRequest, request, response, casAssertion,
                        samlRegisteredService, adaptor, binding, messageContext);
                    val nameID = newSamlObject(NameID.class);
                    nameID.setFormat(nameId.getFormat());
                    nameID.setNameQualifier(nameId.getNameQualifier());
                    nameID.setSPNameQualifier(nameId.getSPNameQualifier());
                    nameID.setValue(nameId.getValue());
                    attributeValue = nameID;
                }
                if (NameID.PERSISTENT.equalsIgnoreCase(valueType)) {
                    val nameID = newSamlObject(NameID.class);
                    nameID.setFormat(NameID.PERSISTENT);
                    nameID.setNameQualifier(SamlIdPUtils.determineNameIdNameQualifier(samlRegisteredService, samlIdPMetadataResolver));
                    FunctionUtils.doIf(StringUtils.isNotBlank(samlRegisteredService.getServiceProviderNameIdQualifier()),
                            value -> nameID.setSPNameQualifier(samlRegisteredService.getServiceProviderNameIdQualifier()),
                            value -> nameID.setSPNameQualifier(adaptor.getEntityId()))
                        .accept(samlRegisteredService);
                    CollectionUtils.firstElement(attributeValue).ifPresent(value -> nameID.setValue(value.toString()));
                    attributeValue = nameID;
                }

                LOGGER.debug("Creating SAML attribute [{}] with value [{}], friendlyName [{}]", name, attributeValue, friendlyName);
                val attribute = newAttribute(friendlyName, name, attributeValue,
                    nameFormats,
                    resp.getDefaultAttributeNameFormat(),
                    samlRegisteredService.getAttributeValueTypes());

                LOGGER.trace("Created SAML attribute [{}] with NameID format [{}]", attribute.getName(), attribute.getNameFormat());
                builder.build(attrStatement, attribute);
            }
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
