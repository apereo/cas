package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link SamlProfileSamlNameIdBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlNameIdBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<NameID> {
    private static final long serialVersionUID = -6231886395225437320L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlProfileSamlNameIdBuilder.class);

    private final PersistentIdGenerator persistentIdGenerator;

    public SamlProfileSamlNameIdBuilder(final OpenSamlConfigBean configBean, final PersistentIdGenerator persistentIdGenerator) {
        super(configBean);
        this.persistentIdGenerator = persistentIdGenerator;
    }

    @Override
    public NameID build(final RequestAbstractType authnRequest, final HttpServletRequest request, 
                        final HttpServletResponse response,
                        final Object assertion, final SamlRegisteredService service,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final String binding) throws SamlException {
        return buildNameId(authnRequest, assertion, service, adaptor);
    }

    /**
     * Build name id.
     * If there are no explicitly defined NameIDFormats, include the default format.
     * see: http://saml2int.org/profile/current/#section92
     *
     * @param authnRequest the authn request
     * @param assertion    the assertion
     * @param service      the service
     * @param adaptor      the adaptor
     * @return the name id
     * @throws SamlException the saml exception
     */
    private NameID buildNameId(final RequestAbstractType authnRequest,
                               final Object assertion,
                               final SamlRegisteredService service,
                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        final List<String> supportedNameFormats = getSupportedNameIdFormats(service, adaptor);
        final String requiredNameFormat = getRequiredNameIdFormatIfAny(authnRequest);
        validateRequiredNameIdFormatIfAny(authnRequest, adaptor, supportedNameFormats, requiredNameFormat);
        final NameID nameid = determineNameId(authnRequest, assertion, supportedNameFormats, service, adaptor);
        return finalizeNameId(nameid, authnRequest, assertion, supportedNameFormats, service, adaptor);
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
     * @return the name id
     */
    protected NameID finalizeNameId(final NameID nameid,
                                    final RequestAbstractType authnRequest,
                                    final Object assertion,
                                    final List<String> supportedNameFormats,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {

        if (StringUtils.isNotBlank(service.getNameIdQualifier())) {
            nameid.setNameQualifier(service.getNameIdQualifier());
        } else {
            final String issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
            nameid.setNameQualifier(issuer);
        }

        if (StringUtils.isNotBlank(service.getServiceProviderNameIdQualifier())) {
            nameid.setSPNameQualifier(service.getServiceProviderNameIdQualifier());
        } else {
            nameid.setSPNameQualifier(adaptor.getEntityId());
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
                    requiredNameFormat, SamlIdPUtils.getIssuerFromSamlRequest(authnRequest),
                    adaptor.getEntityId(), adaptor.getSupportedNameIdFormats());
        }
    }

    /**
     * Gets required name id format if any.
     *
     * @param authnRequest the authn request
     * @return the required name id format if any
     */
    protected String getRequiredNameIdFormatIfAny(final RequestAbstractType authnRequest) {
        String requiredNameFormat = null;
        if (getNameIDPolicy(authnRequest) != null) {
            requiredNameFormat = getNameIDPolicy(authnRequest).getFormat();
            LOGGER.debug("AuthN request indicates [{}] is the required NameID format", requiredNameFormat);
            if (NameID.ENCRYPTED.equals(requiredNameFormat)) {
                LOGGER.warn("Encrypted NameID formats are not supported");
                requiredNameFormat = null;
            }
        }
        return requiredNameFormat;
    }

    private NameIDPolicy getNameIDPolicy(final RequestAbstractType authnRequest) {
        if (authnRequest instanceof AuthnRequest) {
            return AuthnRequest.class.cast(authnRequest).getNameIDPolicy();
        }
        return null;
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
        final List<String> supportedNameFormats = adaptor.getSupportedNameIdFormats();
        LOGGER.debug("Metadata for [{}] declares the following NameIDs [{}]", adaptor.getEntityId(), supportedNameFormats);

        if (supportedNameFormats.isEmpty()) {
            supportedNameFormats.add(NameIDType.TRANSIENT);
            LOGGER.debug("No supported nameId formats could be determined from metadata. Added default [{}]", NameIDType.TRANSIENT);
        }
        if (StringUtils.isNotBlank(service.getRequiredNameIdFormat())) {
            final String fmt = parseAndBuildRequiredNameIdFormat(service);
            supportedNameFormats.add(0, fmt);
            LOGGER.debug("Added required nameId format [{}] based on saml service configuration for [{}]", fmt, service.getServiceId());
        }
        return supportedNameFormats;
    }

    /**
     * Determine name id name id.
     *
     * @param authnRequest         the authn request
     * @param assertion            the assertion
     * @param supportedNameFormats the supported name formats
     * @param service              the service
     * @param adaptor              the adaptor
     * @return the name id
     */
    protected NameID determineNameId(final RequestAbstractType authnRequest,
                                     final Object assertion,
                                     final List<String> supportedNameFormats,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        for (final String nameFormat : supportedNameFormats) {
            LOGGER.debug("Evaluating NameID format [{}]", nameFormat);
            final NameID nameid = encodeNameIdBasedOnNameFormat(authnRequest, assertion, nameFormat, service, adaptor);
            if (nameid != null) {
                return nameid;
            }
        }
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
     * @return the name id
     */
    protected NameID encodeNameIdBasedOnNameFormat(final RequestAbstractType authnRequest,
                                                   final Object assertion,
                                                   final String nameFormat,
                                                   final SamlRegisteredService service,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        try {
            
            if (authnRequest instanceof AttributeQuery) {
                final AttributeQuery query = AttributeQuery.class.cast(authnRequest);
                final NameID nameID = query.getSubject().getNameID();
                nameID.detach();
                return nameID;
            }
            
            final IdPAttribute attribute = prepareNameIdAttribute(assertion, nameFormat, adaptor);
            final SAML2StringNameIDEncoder encoder = prepareNameIdEncoder(authnRequest, nameFormat, attribute, service, adaptor);
            LOGGER.debug("Encoding NameID based on [{}]", nameFormat);
            final NameID nameid = encoder.encode(attribute);
            LOGGER.debug("Final NameID encoded with format [{}] has value [{}]", nameid.getFormat(), nameid.getValue());
            return nameid;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Prepare name id attribute id p attribute.
     *
     * @param casAssertion  the assertion
     * @param nameFormat the name format
     * @param adaptor    the adaptor
     * @return the idp attribute
     */
    protected IdPAttribute prepareNameIdAttribute(final Object casAssertion, final String nameFormat,
                                                  final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {

        final Assertion assertion = Assertion.class.cast(casAssertion);
        final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());

        final String nameIdValue;
        switch (nameFormat.trim()) {
            case NameIDType.TRANSIENT:
                nameIdValue = persistentIdGenerator.generate(assertion.getPrincipal().getName(), adaptor.getEntityId());
                break;
            default:
                nameIdValue = assertion.getPrincipal().getName();
        }

        final IdPAttributeValue<String> value = new StringAttributeValue(nameIdValue);
        LOGGER.debug("NameID attribute value is set to [{}]", value);
        attribute.setValues(CollectionUtils.wrap(value));
        return attribute;
    }

    /**
     * Prepare name id encoder saml 2 string name id encoder.
     *
     * @param authnRequest the authn request
     * @param nameFormat   the name format
     * @param attribute    the attribute
     * @param service      the service
     * @param adaptor      the adaptor
     * @return the saml 2 string name id encoder
     */
    protected SAML2StringNameIDEncoder prepareNameIdEncoder(final RequestAbstractType authnRequest,
                                                            final String nameFormat,
                                                            final IdPAttribute attribute,
                                                            final SamlRegisteredService service,
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
        encoder.setNameFormat(nameFormat);
        if (getNameIDPolicy(authnRequest) != null) {
            final String qualifier = getNameIDPolicy(authnRequest).getSPNameQualifier();
            LOGGER.debug("NameID qualifier is set to [{}]", qualifier);
            encoder.setNameQualifier(qualifier);
        }
        return encoder;
    }

    private static String parseAndBuildRequiredNameIdFormat(final SamlRegisteredService service) {
        final String fmt = service.getRequiredNameIdFormat().trim();
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

}
