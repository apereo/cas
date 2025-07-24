package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlProfileSamlNameIdBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlNameIdBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<SAMLObject> {

    private final PersistentIdGenerator persistentIdGenerator;

    private final MetadataResolver samlIdPMetadataResolver;

    private final SamlIdPObjectEncrypter samlIdPObjectEncrypter;

    public SamlProfileSamlNameIdBuilder(final OpenSamlConfigBean configBean,
                                        final PersistentIdGenerator persistentIdGenerator,
                                        final MetadataResolver samlIdPMetadataResolver,
                                        final SamlIdPObjectEncrypter samlIdPObjectEncrypter) {
        super(configBean);
        this.persistentIdGenerator = persistentIdGenerator;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
        this.samlIdPObjectEncrypter = samlIdPObjectEncrypter;
    }

    protected static List<String> getSupportedNameIdFormats(final SamlProfileBuilderContext context) {
        val supportedNameFormats = new ArrayList<>(context.getAdaptor().getSupportedNameIdFormats());
        LOGGER.debug("Metadata for [{}] declares the following NameIDs [{}]", context.getAdaptor().getEntityId(), supportedNameFormats);

        if (supportedNameFormats.isEmpty()) {
            supportedNameFormats.add(NameIDType.TRANSIENT);
            LOGGER.debug("No supported nameId formats could be determined from metadata. Added default [{}]", NameIDType.TRANSIENT);
        }
        if (StringUtils.isNotBlank(context.getRegisteredService().getRequiredNameIdFormat())) {
            val fmt = parseAndBuildRequiredNameIdFormat(context.getRegisteredService());
            supportedNameFormats.addFirst(fmt);
            LOGGER.debug("Added required nameId format [{}] based on saml service configuration for [{}]",
                fmt, context.getRegisteredService().getServiceId());
        }
        return supportedNameFormats;
    }

    private static String parseAndBuildRequiredNameIdFormat(final SamlRegisteredService service) {
        val fmt = StringUtils.defaultIfBlank(service.getRequiredNameIdFormat(), NameIDType.UNSPECIFIED).trim();
        LOGGER.debug("Required NameID format assigned to service [{}] is [{}]", service.getName(), fmt);

        if (StringUtils.containsIgnoreCase(NameIDType.EMAIL, fmt)) {
            return NameIDType.EMAIL;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.TRANSIENT, fmt)) {
            return NameIDType.TRANSIENT;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.PERSISTENT, fmt)) {
            return NameIDType.PERSISTENT;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.ENTITY, fmt)) {
            return NameIDType.ENTITY;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.X509_SUBJECT, fmt)) {
            return NameIDType.X509_SUBJECT;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.WIN_DOMAIN_QUALIFIED, fmt)) {
            return NameIDType.WIN_DOMAIN_QUALIFIED;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.KERBEROS, fmt)) {
            return NameIDType.KERBEROS;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.ENCRYPTED, fmt)) {
            return NameIDType.ENCRYPTED;
        }
        return NameIDType.UNSPECIFIED;
    }

    protected static String getRequiredNameIdFormatIfAny(final SamlProfileBuilderContext context) {
        val requiredNameFormat = SamlIdPUtils.getNameIDPolicy(context.getSamlRequest()).map(NameIDPolicy::getFormat).orElse(null);
        LOGGER.debug("AuthN request indicates [{}] is the required NameID format", requiredNameFormat);
        return requiredNameFormat;
    }
    
    @Override
    public SAMLObject build(final SamlProfileBuilderContext context) throws SamlException {
        if (context.getSamlRequest() instanceof AttributeQuery) {
            return determineNameIdForAttributeQuery(context);
        }
        return buildNameId(context);
    }

    protected NameID buildNameId(final SamlProfileBuilderContext context) throws SamlException {
        val supportedNameFormats = getSupportedNameIdFormats(context);
        val requiredNameFormat = getRequiredNameIdFormatIfAny(context);
        validateRequiredNameIdFormatIfAny(supportedNameFormats, requiredNameFormat, context);
        val nameID = determineNameId(supportedNameFormats, context);
        return finalizeNameId(nameID, context);
    }

    protected NameID finalizeNameId(final NameID nameid,
                                    final SamlProfileBuilderContext context) {
        if (nameid != null) {
            val registeredService = context.getRegisteredService();
            if (!Strings.CI.equals(registeredService.getNameIdQualifier(), "none")
                && !registeredService.isSkipGeneratingNameIdQualifier()) {
                if (StringUtils.isNotBlank(registeredService.getNameIdQualifier())) {
                    nameid.setNameQualifier(registeredService.getNameIdQualifier());
                } else {
                    nameid.setNameQualifier(SamlIdPUtils.determineNameIdNameQualifier(registeredService, samlIdPMetadataResolver));
                }
            }

            if (!Strings.CI.equals(registeredService.getServiceProviderNameIdQualifier(), "none")
                 && !registeredService.isSkipGeneratingServiceProviderNameIdQualifier()) {
                FunctionUtils.doIf(StringUtils.isNotBlank(registeredService.getServiceProviderNameIdQualifier()),
                        value -> nameid.setSPNameQualifier(registeredService.getServiceProviderNameIdQualifier()),
                        value -> nameid.setSPNameQualifier(context.getAdaptor().getEntityId()))
                    .accept(registeredService);
            }
        }
        return nameid;
    }

    protected void validateRequiredNameIdFormatIfAny(final List<String> supportedNameFormats,
                                                     final String requiredNameFormat,
                                                     final SamlProfileBuilderContext context) {
        if (StringUtils.isNotBlank(requiredNameFormat) && !supportedNameFormats.contains(requiredNameFormat)) {
            LOGGER.warn("Required NameID format [{}] in the AuthN request issued by [{}] is not supported based on the metadata for [{}]. "
                        + "The requested NameID format may not be honored. You should consult the metadata for this service "
                        + "and ensure the requested NameID format is present in the collection of supported "
                        + "metadata formats in the metadata, which are the following: [{}]",
                requiredNameFormat, SamlIdPUtils.getIssuerFromSamlObject(context.getSamlRequest()),
                context.getAdaptor().getEntityId(), context.getAdaptor().getSupportedNameIdFormats());
        }
    }

    protected NameID determineNameId(final List<String> supportedNameFormats, final SamlProfileBuilderContext context) {
        for (val nameFormat : supportedNameFormats) {
            LOGGER.debug("Evaluating NameID format [{}]", nameFormat);
            val nameId = encodeNameIdBasedOnNameFormat(context, nameFormat);
            if (nameId != null) {
                LOGGER.debug("Determined NameID based on format [{}] to be [{}]", nameFormat, nameId.getValue());
                return nameId;
            }
        }
        LOGGER.warn("No NameID could be determined based on the supported formats [{}]", supportedNameFormats);
        return null;
    }

    protected NameID encodeNameIdBasedOnNameFormat(final SamlProfileBuilderContext context,
                                                   final String nameFormat) {
        try {
            val attribute = prepareNameIdAttribute(context, nameFormat);
            val encoder = SamlAttributeBasedNameIdGenerator.get(Optional.of(context.getSamlRequest()),
                nameFormat, context.getRegisteredService(), attribute);
            context.getHttpRequest().setAttribute(NameID.class.getName(), attribute);
            LOGGER.debug("Encoding NameID based on [{}]", nameFormat);
            val prc = new ProfileRequestContext();
            val nameId = Objects.requireNonNull(encoder.generate(prc, nameFormat));
            LOGGER.debug("Final NameID encoded with format [{}] has value [{}]", nameId.getFormat(), nameId.getValue());
            return nameId;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Prepare name id attribute idp attribute.
     *
     * @param context    the context
     * @param nameFormat the name format
     * @return the idp attribute
     */
    protected String prepareNameIdAttribute(final SamlProfileBuilderContext context,
                                            final String nameFormat) {
        val principalId = context.getAuthenticatedAssertion().orElseThrow().getName();
        LOGGER.debug("Preparing NameID attribute for principal [{}]", principalId);
        val nameIdValue = getNameIdValueFromNameFormat(nameFormat, context);
        LOGGER.debug("NameID attribute value is set to [{}]", nameIdValue);
        return nameIdValue;
    }

    private String getNameIdValueFromNameFormat(final String nameFormat,
                                                final SamlProfileBuilderContext context) {
        val principalId = context.getAuthenticatedAssertion().orElseThrow().getName();
        if (NameIDType.TRANSIENT.equalsIgnoreCase(StringUtils.trim(nameFormat))) {
            val entityId = context.getAdaptor().getEntityId();
            if (context.getRegisteredService().isSkipGeneratingTransientNameId()) {
                LOGGER.debug("Generation of transient NameID value is skipped for [{}] and [{}] will be used instead", entityId, principalId);
            } else {
                LOGGER.debug("Generating transient NameID value for principal [{}] and entity id [{}]", principalId, entityId);
                return persistentIdGenerator.generate(principalId, entityId);
            }
        }
        return principalId;
    }

    private SAMLObject determineNameIdForAttributeQuery(final SamlProfileBuilderContext context) {
        val query = (AttributeQuery) context.getSamlRequest();
        val result = query.getSubject().getNameID() == null
            ? samlIdPObjectEncrypter.decode(query.getSubject().getEncryptedID(), context.getRegisteredService(), context.getAdaptor())
            : query.getSubject().getNameID();
        result.detach();
        return result;
    }
}
