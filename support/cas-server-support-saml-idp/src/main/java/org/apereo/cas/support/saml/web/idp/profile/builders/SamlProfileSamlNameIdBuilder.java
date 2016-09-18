package org.apereo.cas.support.saml.web.idp.profile.builders;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public NameID build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                              final Assertion assertion, final SamlRegisteredService service,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildNameId(authnRequest, assertion, service, adaptor);
    }

    private NameID buildNameId(final AuthnRequest authnRequest, final Assertion assertion,
                               final SamlRegisteredService service, final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {

        final List<String> supportedNameFormats = adaptor.getSupportedNameFormats();
        logger.debug("Metadata for [{}] declares support for the following NameIDs [{}]", adaptor.getEntityId(), supportedNameFormats);

        // there are no explicitly defined NameIDFormats, include the default format
        // see: http://saml2int.org/profile/current/#section92
        if (supportedNameFormats.isEmpty()) {
            supportedNameFormats.add(NameIDType.TRANSIENT);
        }
        if (StringUtils.isNotBlank(service.getRequiredNameIdFormat())) {
            supportedNameFormats.add(0, service.getRequiredNameIdFormat());
        }

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            logger.debug("AuthN request says [{}] is the required NameID format", requiredNameFormat);
            if (NameID.ENCRYPTED.equals(requiredNameFormat)) {
                logger.warn("Encrypted NameID formats are not supported");
                requiredNameFormat = null;
            }
        }
        
        if (StringUtils.isNotBlank(requiredNameFormat) && !supportedNameFormats.contains(requiredNameFormat)) {
            logger.warn("Required NameID format [{}] in the AuthN request issued by [{}] is not supported based on the metadata for [{}]",
                    requiredNameFormat, SamlIdPUtils.getIssuerFromSamlRequest(authnRequest), adaptor.getEntityId());
            throw new SamlException("Required NameID format cannot be provided because it is not supported");
        }

        try {
            for (final String nameFormat : supportedNameFormats) {
                logger.debug("Evaluating NameID format {}", nameFormat);
                
                final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
                encoder.setNameFormat(nameFormat);
                if (authnRequest.getNameIDPolicy() != null) {
                    final String qualifier = authnRequest.getNameIDPolicy().getSPNameQualifier();
                    logger.debug("NameID qualifier is set to {}", qualifier);
                    encoder.setNameQualifier(qualifier);
                }
                final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
                final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
                logger.debug("NameID attribute value is set to {}", assertion.getPrincipal().getName());
                attribute.setValues(Collections.singletonList(value));
                logger.debug("Encoding NameID based on {}", nameFormat);
                
                final NameID nameid = encoder.encode(attribute);
                logger.debug("Final NameID encoded is {} with value {}", nameid.getFormat(), nameid.getValue());
                return nameid;
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
}
