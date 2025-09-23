package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.apache.commons.lang3.Strings;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlAttributeStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AttributeStatement> {

    private final CasConfigurationProperties casProperties;

    private final SamlIdPObjectEncrypter samlObjectEncrypter;

    private final AttributeDefinitionStore attributeDefinitionStore;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final SamlProfileObjectBuilder<SAMLObject> samlNameIdBuilder;

    private final MetadataResolver samlIdPMetadataResolver;

    public SamlProfileSamlAttributeStatementBuilder(final OpenSamlConfigBean configBean,
                                                    final CasConfigurationProperties casProperties,
                                                    final SamlIdPObjectEncrypter samlObjectEncrypter,
                                                    final AttributeDefinitionStore attributeDefinitionStore,
                                                    final ServiceFactory<WebApplicationService> serviceFactory,
                                                    final SamlProfileObjectBuilder<SAMLObject> samlNameIdBuilder,
                                                    final MetadataResolver samlIdPMetadataResolver) {
        super(configBean);
        this.casProperties = casProperties;
        this.samlObjectEncrypter = samlObjectEncrypter;
        this.attributeDefinitionStore = attributeDefinitionStore;
        this.serviceFactory = serviceFactory;
        this.samlNameIdBuilder = samlNameIdBuilder;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
    }

    @Override
    public AttributeStatement build(final SamlProfileBuilderContext context) throws Exception {
        val attributes = new HashMap<>(context.getAuthenticatedAssertion().orElseThrow().getAttributes());
        val webApplicationService = serviceFactory.createService(context.getAdaptor().getEntityId(), WebApplicationService.class);
        val encodedAttrs = ProtocolAttributeEncoder.decodeAttributes(attributes, context.getRegisteredService(), webApplicationService);

        val attrBuilder = new SamlProfileSamlRegisteredServiceAttributeBuilder(
            context.getRegisteredService(), context.getAdaptor(), samlObjectEncrypter);
        return newAttributeStatement(context, encodedAttrs, attrBuilder);
    }

    private String getAttributeFriendlyName(final SamlProfileBuilderContext context, final String name) {
        if (context.getRegisteredService().getAttributeFriendlyNames().containsKey(name)) {
            return context.getRegisteredService().getAttributeFriendlyNames().get(name);
        }
        return attributeDefinitionStore.getAttributeDefinitionsBy(SamlIdPAttributeDefinition.class)
            .filter(defn -> Strings.CI.equals(name, defn.getKey())
                            || Strings.CI.equals(name, defn.getName())
                            || Strings.CI.equals(name, defn.getUrn()))
            .findFirst()
            .map(SamlIdPAttributeDefinition::getFriendlyName)
            .filter(StringUtils::isNotBlank)
            .stream()
            .findFirst()
            .or(() -> {
                val globalFriendlyNames = casProperties.getAuthn().getSamlIdp().getCore().getAttributeFriendlyNames();
                val friendlyNames = new HashMap<>(CollectionUtils.convertDirectedListToMap(globalFriendlyNames));
                return Optional.ofNullable(friendlyNames.get(name));
            })
            .orElse(name);
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

        val resp = casProperties.getAuthn().getSamlIdp().getResponse();
        val nameFormats = new HashMap<>(resp.configureAttributeNameFormats());
        nameFormats.putAll(context.getRegisteredService().getAttributeNameFormats());

        val urns = new HashMap<String, String>();
        attributeDefinitionStore.getAttributeDefinitions()
            .stream()
            .filter(SamlIdPAttributeDefinition.class::isInstance)
            .map(SamlIdPAttributeDefinition.class::cast)
            .filter(defn -> StringUtils.isNotBlank(defn.getUrn()))
            .forEach(defn -> {
                urns.put(defn.getKey(), defn.getUrn());
                urns.put(defn.getName(), defn.getUrn());
            });
        LOGGER.debug("Attribute definitions tagged with URNs in the attribute definition store are [{}]", urns);
        LOGGER.debug("Attributes to process for SAML2 attribute statement are [{}]", attributes);
        for (val entry : attributes.entrySet()) {
            var attributeValue = entry.getValue();
            var attributeName = entry.getKey();
            
            if (attributeName.equalsIgnoreCase(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())) {
                attributeValue = buildAuthenticationContextClassAttribute(context).orElse(null);
            }
            if (attributeValue == null || (attributeValue instanceof final Collection col && col.isEmpty())) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", attributeName);
                continue;
            }
            
            val friendlyName = getAttributeFriendlyName(context, attributeName);
            val attributeNames = urns.containsKey(attributeName)
                ? List.of(urns.get(attributeName))
                : getMappedAttributeNamesFromAttributeDefinitionStore(attributeName);

            for (val name : attributeNames) {
                LOGGER.trace("Processing SAML2 attribute [{}] with value [{}], friendlyName [{}]", name, attributeValue, friendlyName);
                val valueType = context.getRegisteredService().getAttributeValueTypes().get(name);

                if (NameIDType.class.getSimpleName().equalsIgnoreCase(valueType)) {
                    val nameIdObject = samlNameIdBuilder.build(context);
                    if (nameIdObject instanceof final NameID nameId) {
                        val nameID = newSamlObject(NameID.class);
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

                LOGGER.debug("Creating SAML2 attribute [{}] with value [{}], friendlyName [{}]", name, attributeValue, friendlyName);
                val attribute = newAttribute(friendlyName, name, attributeValue,
                    nameFormats,
                    resp.getDefaultAttributeNameFormat(),
                    context.getRegisteredService().getAttributeValueTypes());

                LOGGER.trace("Created SAML2 attribute [{}] with NameID format [{}]", attribute.getName(), attribute.getNameFormat());
                builder.build(attrStatement, attribute);
            }
        }
        return attrStatement;
    }

    protected Collection<String> getMappedAttributeNamesFromAttributeDefinitionStore(final String entry) {
        return org.springframework.util.StringUtils.commaDelimitedListToSet(
            attributeDefinitionStore.locateAttributeDefinition(entry)
                .map(AttributeDefinition::getName)
                .filter(StringUtils::isNotBlank)
                .orElse(entry));
    }

    protected Optional<String> buildAuthenticationContextClassAttribute(final SamlProfileBuilderContext context) {
        val contextValues = CollectionUtils.toCollection(context.getAuthenticatedAssertion()
            .orElseThrow().getAttributes().get(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute()));
        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getContext().getAuthenticationContextClassMappings());
        return definedContexts.entrySet()
            .stream()
            .filter(entry -> contextValues.contains(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst();
    }
}
