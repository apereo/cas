package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.util.Saml20AttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;

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

    private final SamlProfileObjectBuilder<SAMLObject> samlNameIdBuilder;

    private final MetadataResolver samlIdPMetadataResolver;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final SamlIdPProperties samlIdPProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter,
                                                    final AttributeDefinitionStore attributeDefinitionStore,
                                                    final ServiceFactory<WebApplicationService> serviceFactory,
                                                    final SamlProfileObjectBuilder<SAMLObject> samlNameIdBuilder,
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
    public AttributeStatement build(final SamlProfileBuilderContext context) throws Exception {
        val attributes = new HashMap<>(context.getAuthenticatedAssertion().getAttributes());
        val webApplicationService = serviceFactory.createService(context.getAdaptor().getEntityId(), WebApplicationService.class);
        val encodedAttrs = ProtocolAttributeEncoder.decodeAttributes(attributes, context.getRegisteredService(), webApplicationService);

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(
            context.getRegisteredService(), context.getAdaptor(), samlObjectEncrypter);
        return newAttributeStatement(context, encodedAttrs, attrBuilder);
    }

    /**
     * New attribute statement.
     *
     * @param context    the context
     * @param attributes the attributes
     * @param builder    the builder
     * @return the attribute statement
     * @throws Exception the exception
     */
    public AttributeStatement newAttributeStatement(final SamlProfileBuilderContext context,
                                                    final Map<String, Object> attributes,
                                                    final Saml20AttributeBuilder builder) throws Exception {
        val attrStatement = SamlUtils.newSamlObject(AttributeStatement.class);

        val resp = samlIdPProperties.getResponse();
        val nameFormats = new HashMap<>(resp.configureAttributeNameFormats());
        nameFormats.putAll(context.getRegisteredService().getAttributeNameFormats());

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

        friendlyNames.putAll(context.getRegisteredService().getAttributeFriendlyNames());

        SamlIdPAttributeDefinitionCatalog.load()
            .filter(defn -> !friendlyNames.containsKey(defn.getKey()))
            .forEach(defn -> {
                friendlyNames.put(defn.getKey(), defn.getFriendlyName());
                urns.put(defn.getKey(), defn.getUrn());
            });

        LOGGER.debug("Attributes to process for SAML2 attribute statement are [{}]", attributes);
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
                val valueType = context.getRegisteredService().getAttributeValueTypes().get(name);

                if (NameIDType.class.getSimpleName().equalsIgnoreCase(valueType)) {
                    val nameIdObject = samlNameIdBuilder.build(context);
                    if (nameIdObject instanceof NameID) {
                        val nameID = newSamlObject(NameID.class);
                        val nameId = (NameID) nameIdObject;
                        nameID.setFormat(nameId.getFormat());
                        nameID.setNameQualifier(nameId.getNameQualifier());
                        nameID.setSPNameQualifier(nameId.getSPNameQualifier());
                        nameID.setValue(nameId.getValue());
                        attributeValue = nameID;
                    }
                }
                if (NameIDType.PERSISTENT.equalsIgnoreCase(valueType)) {
                    val nameID = newSamlObject(NameID.class);
                    nameID.setFormat(NameIDType.PERSISTENT);
                    nameID.setNameQualifier(SamlIdPUtils.determineNameIdNameQualifier(context.getRegisteredService(), samlIdPMetadataResolver));
                    FunctionUtils.doIf(StringUtils.isNotBlank(context.getRegisteredService().getServiceProviderNameIdQualifier()),
                            value -> nameID.setSPNameQualifier(context.getRegisteredService().getServiceProviderNameIdQualifier()),
                            value -> nameID.setSPNameQualifier(context.getAdaptor().getEntityId()))
                        .accept(context.getRegisteredService());
                    CollectionUtils.firstElement(attributeValue).ifPresent(value -> nameID.setValue(value.toString()));
                    attributeValue = nameID;
                }

                LOGGER.debug("Creating SAML attribute [{}] with value [{}], friendlyName [{}]", name, attributeValue, friendlyName);
                val attribute = newAttribute(friendlyName, name, attributeValue,
                    nameFormats,
                    resp.getDefaultAttributeNameFormat(),
                    context.getRegisteredService().getAttributeValueTypes());

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
