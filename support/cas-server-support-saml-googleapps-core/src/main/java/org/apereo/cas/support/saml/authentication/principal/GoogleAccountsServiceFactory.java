package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Builds {@link GoogleAccountsService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 * @deprecated Since 6.2, to be replaced with CAS SAML2 identity provider functionality.
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "6.2.0")
public class GoogleAccountsServiceFactory extends AbstractServiceFactory<GoogleAccountsService> {
    private final GoogleSaml20ObjectBuilder googleSaml20ObjectBuilder;

    @Override
    public GoogleAccountsService createService(final HttpServletRequest request) {
        val relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);

        val samlRequest = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
        var xmlRequest = StringUtils.EMPTY;
        try {
            LOGGER.trace("Decoding saml authentication request [{}]", samlRequest);
            xmlRequest = this.googleSaml20ObjectBuilder.decodeSamlAuthnRequest(samlRequest);
        } catch (final Exception e) {
            LOGGER.debug("Unable to decode SAML authentication request", e);
            xmlRequest = null;
        }

        if (StringUtils.isBlank(xmlRequest)) {
            LOGGER.trace("SAML authentication request not found in the request");
            return null;
        }

        val document = GoogleSaml20ObjectBuilder.constructDocumentFromXml(xmlRequest);
        if (document == null) {
            LOGGER.warn("Unable to construct XML document from request");
            return null;
        }

        val root = document.getRootElement();
        val assertionConsumerServiceUrl = root.getAttributeValue(SamlProtocolConstants.PARAMETER_SAML_ACS_URL);
        val requestId = root.getAttributeValue("ID");
        val s = new GoogleAccountsService(assertionConsumerServiceUrl, relayState, requestId);
        s.setLoggedOutAlready(true);
        s.setSource(SamlProtocolConstants.PARAMETER_SAML_ACS_URL);
        return s;
    }

    @Override
    public GoogleAccountsService createService(final String id) {
        throw new NotImplementedException("This operation is not supported.");
    }
}
