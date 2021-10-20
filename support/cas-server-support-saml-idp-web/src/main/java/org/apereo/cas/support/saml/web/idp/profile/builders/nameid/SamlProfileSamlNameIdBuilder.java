package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class SamlProfileSamlNameIdBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<NameID> {
    private static final long serialVersionUID = -6231886395225437320L;

    private final PersistentIdGenerator persistentIdGenerator;

    private final MetadataResolver samlIdPMetadataResolver;

    public SamlProfileSamlNameIdBuilder(final OpenSamlConfigBean configBean,
                                        final PersistentIdGenerator persistentIdGenerator,
                                        final MetadataResolver samlIdPMetadataResolver
    ) {
        super(configBean);
        this.persistentIdGenerator = persistentIdGenerator;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
    }

    /**
     * Gets supported name id formats.
     *
     * @param service the service
     * @param adaptor the adaptor
     * @return the supported name id formats
     */
    protected static List<String> getSupportedNameIdFormats(final SamlRegisteredService service,
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val supportedNameFormats = new ArrayList<>(adaptor.getSupportedNameIdFormats());
        LOGGER.debug("Metadata for [{}] declares the following NameIDs [{}]", adaptor.getEntityId(), supportedNameFormats);

        if (supportedNameFormats.isEmpty()) {
            supportedNameFormats.add(NameIDType.TRANSIENT);
            LOGGER.debug("No supported nameId formats could be determined from metadata. Added default [{}]", NameIDType.TRANSIENT);
        }
        if (StringUtils.isNotBlank(service.getRequiredNameIdFormat())) {
            val fmt = parseAndBuildRequiredNameIdFormat(service);
            supportedNameFormats.add(0, fmt);
            LOGGER.debug("Added required nameId format [{}] based on saml service configuration for [{}]", fmt, service.getServiceId());
        }
        return supportedNameFormats;
    }

    private static String parseAndBuildRequiredNameIdFormat(final SamlRegisteredService service) {
        val fmt = StringUtils.defaultString(service.getRequiredNameIdFormat(), NameIDType.UNSPECIFIED).trim();
        LOGGER.debug("Required NameID format assigned to service [{}] is [{}]", service.getName(), fmt);

        if (StringUtils.containsIgnoreCase(NameIDType.EMAIL, fmt)) {
            return NameIDType.EMAIL;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.TRANSIENT, fmt)) {
            return NameIDType.TRANSIENT;
        }
        if (StringUtils.containsIgnoreCase(NameIDType.UNSPECIFIED, fmt)) {
            return NameIDType.UNSPECIFIED;
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
        return fmt;
    }

    /**
     * Gets required name id format if any.
     *
     * @param authnRequest the authn request
     * @return the required name id format if any
     */
    protected static String getRequiredNameIdFormatIfAny(final RequestAbstractType authnRequest) {
        val requiredNameFormat = SamlIdPUtils.getNameIDPolicy(authnRequest).map(NameIDPolicy::getFormat).orElse(null);
        LOGGER.debug("AuthN request indicates [{}] is the required NameID format", requiredNameFormat);
        return requiredNameFormat;
    }


    @Override
    public NameID build(final RequestAbstractType authnRequest,
                        final HttpServletRequest request,
                        final HttpServletResponse response,
                        final AuthenticatedAssertionContext assertion,
                        final SamlRegisteredService service,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final String binding,
                        final MessageContext messageContext) throws SamlException {
        return buildNameId(authnRequest, assertion, service, adaptor, messageContext, request);
    }

    /**
     * Build name id.
     * If there are no explicitly defined NameIDFormats, include the default format.
     * see: http://saml2int.org/profile/current/#section92
     *
     * @param authnRequest   the authn request
     * @param assertion      the assertion
     * @param service        the service
     * @param adaptor        the adaptor
     * @param messageContext the message context
     * @param request        the request
     * @return the name id
     * @throws SamlException the saml exception
     */
    protected NameID buildNameId(final RequestAbstractType authnRequest,
                                 final AuthenticatedAssertionContext assertion,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                 final MessageContext messageContext,
                                 final HttpServletRequest request) throws SamlException {
        val supportedNameFormats = getSupportedNameIdFormats(service, adaptor);
        val requiredNameFormat = getRequiredNameIdFormatIfAny(authnRequest);
        validateRequiredNameIdFormatIfAny(authnRequest, adaptor, supportedNameFormats, requiredNameFormat);
        val nameID = determineNameId(authnRequest, assertion, supportedNameFormats, service, adaptor, request);
        return finalizeNameId(nameID, authnRequest, assertion, supportedNameFormats, service, adaptor, request);
    }

    /**
     * Finalize name id name id.
     *
     * @param nameid               the nameid
     * @param authnRequest         the authn request
     * @param assertion            the assertion
     * @param supportedNameFormats the supported name formats
     * @param service              the service
     * @param adaptor              the adaptor
     * @param request              the request
     * @return the name id
     */
    protected NameID finalizeNameId(final NameID nameid,
                                    final RequestAbstractType authnRequest,
                                    final AuthenticatedAssertionContext assertion,
                                    final List<String> supportedNameFormats,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final HttpServletRequest request) {

        if (nameid != null) {
            if (StringUtils.isNotBlank(service.getNameIdQualifier())) {
                nameid.setNameQualifier(service.getNameIdQualifier());
            } else {
                nameid.setNameQualifier(SamlIdPUtils.determineNameIdNameQualifier(service, samlIdPMetadataResolver));
                FunctionUtils.doIf(StringUtils.isNotBlank(service.getServiceProviderNameIdQualifier()),
                        value -> nameid.setSPNameQualifier(service.getServiceProviderNameIdQualifier()),
                        value -> nameid.setSPNameQualifier(adaptor.getEntityId()))
                    .accept(service);
            }
        }
        return nameid;
    }

    /**
     * Validate required name id format if any.
     *
     * @param authnRequest         the authn request
     * @param adaptor              the adaptor
     * @param supportedNameFormats the supported name formats
     * @param requiredNameFormat   the required name format
     */
    protected void validateRequiredNameIdFormatIfAny(final RequestAbstractType authnRequest,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final List<String> supportedNameFormats,
                                                     final String requiredNameFormat) {
        if (StringUtils.isNotBlank(requiredNameFormat) && !supportedNameFormats.contains(requiredNameFormat)) {
            LOGGER.warn("Required NameID format [{}] in the AuthN request issued by [{}] is not supported based on the metadata for [{}]. "
                        + "The requested NameID format may not be honored. You should consult the metadata for this service "
                        + "and ensure the requested NameID format is present in the collection of supported "
                        + "metadata formats in the metadata, which are the following: [{}]",
                requiredNameFormat, SamlIdPUtils.getIssuerFromSamlObject(authnRequest),
                adaptor.getEntityId(), adaptor.getSupportedNameIdFormats());
        }
    }

    /**
     * Determine name id name id.
     *
     * @param authnRequest         the authn request
     * @param assertion            the assertion
     * @param supportedNameFormats the supported name formats
     * @param service              the service
     * @param adaptor              the adaptor
     * @param request              the request
     * @return the name id
     */
    protected NameID determineNameId(final RequestAbstractType authnRequest,
                                     final AuthenticatedAssertionContext assertion,
                                     final List<String> supportedNameFormats,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request) {
        for (val nameFormat : supportedNameFormats) {
            LOGGER.debug("Evaluating NameID format [{}]", nameFormat);
            val nameId = encodeNameIdBasedOnNameFormat(authnRequest, assertion, nameFormat, service, adaptor, request);
            if (nameId != null) {
                LOGGER.debug("Determined NameID based on format [{}] to be [{}]", nameFormat, nameId.getValue());
                return nameId;
            }
        }
        LOGGER.warn("No NameID could be determined based on the supported formats [{}]", supportedNameFormats);
        return null;
    }

    /**
     * Encode name id based on name format name id.
     *
     * @param authnRequest the authn request
     * @param assertion    the assertion
     * @param nameFormat   the name format
     * @param service      the service
     * @param adaptor      the adaptor
     * @param request      the request
     * @return the name id
     */
    protected NameID encodeNameIdBasedOnNameFormat(final RequestAbstractType authnRequest,
                                                   final AuthenticatedAssertionContext assertion,
                                                   final String nameFormat,
                                                   final SamlRegisteredService service,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                   final HttpServletRequest request) {
        try {
            val attribute = prepareNameIdAttribute(assertion, nameFormat, adaptor, service);
            val encoder = SamlAttributeBasedNameIdGenerator.get(Optional.of(authnRequest), nameFormat, service, attribute);
            request.setAttribute(NameID.class.getName(), attribute);
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
     * @param assertion         the assertion
     * @param nameFormat        the name format
     * @param adaptor           the adaptor
     * @param registeredService the registered service
     * @return the idp attribute
     */
    protected String prepareNameIdAttribute(final AuthenticatedAssertionContext assertion,
                                            final String nameFormat,
                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                            final SamlRegisteredService registeredService) {
        LOGGER.debug("Preparing NameID attribute for principal [{}]", assertion.getName());
        val nameIdValue = getNameIdValueFromNameFormat(nameFormat, adaptor, assertion.getName(), registeredService);
        LOGGER.debug("NameID attribute value is set to [{}]", nameIdValue);
        return nameIdValue;
    }

    private String getNameIdValueFromNameFormat(final String nameFormat,
                                                final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                final String principalName,
                                                final SamlRegisteredService registeredService) {
        if (nameFormat.trim().equalsIgnoreCase(NameIDType.TRANSIENT)) {
            val entityId = adaptor.getEntityId();
            if (registeredService.isSkipGeneratingTransientNameId()) {
                LOGGER.debug("Generation of transient NameID value is skipped for [{}] and [{}] will be used instead", entityId, principalName);
            } else {
                LOGGER.debug("Generating transient NameID value for principal [{}] and entity id [{}]", principalName, entityId);
                return persistentIdGenerator.generate(principalName, entityId);
            }
        }
        return principalName;
    }
}
