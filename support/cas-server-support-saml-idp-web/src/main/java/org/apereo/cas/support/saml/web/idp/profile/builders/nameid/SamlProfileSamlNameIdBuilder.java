package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import module java.base;
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

/**
 * This is {@link SamlProfileSamlNameIdBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlNameIdBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<SAMLObject> {

    private static final Set<String> NAME_ID_FORMATS_WITHOUT_CONSTRAINT = Set.of(NameIDType.UNSPECIFIED,
        "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified", NameIDType.ENCRYPTED);

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

        if (Strings.CI.contains(NameIDType.EMAIL, fmt)) {
            return NameIDType.EMAIL;
        }
        if (Strings.CI.contains(NameIDType.TRANSIENT, fmt)) {
            return NameIDType.TRANSIENT;
        }
        if (Strings.CI.contains(NameIDType.PERSISTENT, fmt)) {
            return NameIDType.PERSISTENT;
        }
        if (Strings.CI.contains(NameIDType.ENTITY, fmt)) {
            return NameIDType.ENTITY;
        }
        if (Strings.CI.contains(NameIDType.X509_SUBJECT, fmt)) {
            return NameIDType.X509_SUBJECT;
        }
        if (Strings.CI.contains(NameIDType.WIN_DOMAIN_QUALIFIED, fmt)) {
            return NameIDType.WIN_DOMAIN_QUALIFIED;
        }
        if (Strings.CI.contains(NameIDType.KERBEROS, fmt)) {
            return NameIDType.KERBEROS;
        }
        if (Strings.CI.contains(NameIDType.ENCRYPTED, fmt)) {
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
        val nameFormats = determineNameIdFormats(supportedNameFormats, requiredNameFormat, context);
        val nameID = determineNameId(nameFormats, context);
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

    /**
     * Determine the NameID formats to try, in order. A format requested by the {@code NameIDPolicy} of the
     * request, other than {@code unspecified} or {@code encrypted}, is the only one tried, as SAML2 core
     * section 3.4.1.1 requires the NameID to carry exactly that format; a requested format the service provider
     * does not support is refused. A NameID format required by the service takes precedence over the request.
     *
     * @param supportedNameFormats the supported name formats
     * @param requestedNameFormat  the name format requested by the request, if any
     * @param context              the context
     * @return the NameID formats to try
     * @throws SamlIdPInvalidNameIdPolicyException when the requested format is not supported
     */
    protected List<String> determineNameIdFormats(final List<String> supportedNameFormats,
                                                  final String requestedNameFormat,
                                                  final SamlProfileBuilderContext context) {
        if (StringUtils.isBlank(requestedNameFormat)
            || NAME_ID_FORMATS_WITHOUT_CONSTRAINT.contains(requestedNameFormat.trim())) {
            return supportedNameFormats;
        }
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(context.getSamlRequest());
        val requiredNameIdFormat = context.getRegisteredService().getRequiredNameIdFormat();
        if (StringUtils.isNotBlank(requiredNameIdFormat)) {
            LOGGER.debug("NameID format [{}] requested by [{}] is ignored, since the service requires [{}]",
                requestedNameFormat, issuer, requiredNameIdFormat);
            return supportedNameFormats;
        }
        val nameFormat = requestedNameFormat.trim();
        if (supportedNameFormats.stream().map(StringUtils::trim).anyMatch(nameFormat::equals)) {
            LOGGER.debug("Using NameID format [{}] requested by [{}]", nameFormat, issuer);
            return List.of(nameFormat);
        }
        LOGGER.warn("NameID format [{}] requested by [{}] is not supported for [{}], which supports [{}]",
            nameFormat, issuer, context.getAdaptor().getEntityId(), supportedNameFormats);
        throw new SamlIdPInvalidNameIdPolicyException("NameID format %s requested by %s is not supported"
            .formatted(nameFormat, context.getAdaptor().getEntityId()));
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
