package org.jasig.cas.support.saml.web.idp.profile.builders;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SamlProfileSamlNameIdBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("samlProfileSamlNameIdBuilder")
public class SamlProfileSamlNameIdBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<NameID> {
    private static final long serialVersionUID = -6231886395225437320L;

    @Override
    public final NameID build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
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

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            logger.debug("AuthN request says [{}] is the required NameID format", requiredNameFormat);
            if (NameID.ENCRYPTED.equals(requiredNameFormat)) {
                logger.warn("Encrypted NameID formats are not supported");
                requiredNameFormat = null;
            }
        }

        final Map<String, Object> principalAttributes = assertion.getPrincipal().getAttributes();
        if (principalAttributes.isEmpty() && StringUtils.isNotBlank(requiredNameFormat)) {
            logger.warn("No CAS attributes for CAS principal [{}], so no name identifier will be created.",
                    assertion.getPrincipal().getName());
            throw new SamlException("No attributes for principal, so NameID format required can not be supported");
        }

        if (StringUtils.isNotBlank(requiredNameFormat) && !supportedNameFormats.contains(requiredNameFormat)) {
            logger.warn("Required NameID format [{}] in the AuthN request issued by [{}] is not supported based on the metadata for [{}]",
                    requiredNameFormat, authnRequest.getIssuer().getValue(), adaptor.getEntityId());
            throw new SamlException("Required NameID format cannot be provided because it is not supported");
        }

        try {
            for (final String nameFormat : supportedNameFormats) {
                final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
                encoder.setNameFormat(nameFormat);
                if (authnRequest.getNameIDPolicy() != null) {
                    encoder.setNameQualifier(authnRequest.getNameIDPolicy().getSPNameQualifier());
                }
                final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
                final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
                attribute.setValues(Collections.singletonList(value));
                return encoder.encode(attribute);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
