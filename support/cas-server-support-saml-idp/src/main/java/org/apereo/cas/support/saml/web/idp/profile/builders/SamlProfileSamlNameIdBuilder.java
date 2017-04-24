package org.apereo.cas.support.saml.web.idp.profile.builders;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
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

    public SamlProfileSamlNameIdBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    @Override
    public NameID build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                        final Assertion assertion, final SamlRegisteredService service,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final String binding)
            throws SamlException {
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
    private NameID buildNameId(final AuthnRequest authnRequest,
                               final Assertion assertion,
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
                                    final AuthnRequest authnRequest,
                                    final Assertion assertion,
                                    final List<String> supportedNameFormats,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        
        if (StringUtils.isNotBlank(service.getNameIdQualifier())) {
            nameid.setNameQualifier(service.getNameIdQualifier());
        }
        if (StringUtils.isNotBlank(service.getServiceProviderNameIdQualifier())) {
            nameid.setNameQualifier(service.getServiceProviderNameIdQualifier());
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
    protected void validateRequiredNameIdFormatIfAny(final AuthnRequest authnRequest,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                     final List<String> supportedNameFormats,
                                                     final String requiredNameFormat) {
        if (StringUtils.isNotBlank(requiredNameFormat) && !supportedNameFormats.contains(requiredNameFormat)) {
            LOGGER.warn("Required NameID format [{}] in the AuthN request issued by [{}] is not supported based on the metadata for [{}]",
                    requiredNameFormat, SamlIdPUtils.getIssuerFromSamlRequest(authnRequest), adaptor.getEntityId());
            throw new SamlException("Unsupported required NameID format cannot be provided");
        }
    }

    /**
     * Gets required name id format if any.
     *
     * @param authnRequest the authn request
     * @return the required name id format if any
     */
    protected String getRequiredNameIdFormatIfAny(final AuthnRequest authnRequest) {
        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            LOGGER.debug("AuthN request indicates [{}] is the required NameID format", requiredNameFormat);
            if (NameID.ENCRYPTED.equals(requiredNameFormat)) {
                LOGGER.warn("Encrypted NameID formats are not supported");
                requiredNameFormat = null;
            }
        }
        return requiredNameFormat;
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
    protected NameID determineNameId(final AuthnRequest authnRequest,
                                     final Assertion assertion,
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
    protected NameID encodeNameIdBasedOnNameFormat(final AuthnRequest authnRequest,
                                                   final Assertion assertion,
                                                   final String nameFormat,
                                                   final SamlRegisteredService service,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        try {
            final IdPAttribute attribute = prepareNameIdAttribute(assertion);
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
     * @param assertion the assertion
     * @return the id p attribute
     */
    protected IdPAttribute prepareNameIdAttribute(final Assertion assertion) {
        final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
        final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
        LOGGER.debug("NameID attribute value is set to [{}]", assertion.getPrincipal().getName());
        attribute.setValues(Collections.singletonList(value));
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
    protected SAML2StringNameIDEncoder prepareNameIdEncoder(final AuthnRequest authnRequest,
                                                            final String nameFormat,
                                                            final IdPAttribute attribute,
                                                            final SamlRegisteredService service,
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
        encoder.setNameFormat(nameFormat);
        if (authnRequest.getNameIDPolicy() != null) {
            final String qualifier = authnRequest.getNameIDPolicy().getSPNameQualifier();
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
