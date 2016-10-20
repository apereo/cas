package org.apereo.cas.support.saml.authentication.principal;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jdom.Document;
import org.jdom.Element;

import javax.servlet.http.HttpServletRequest;

/**
 * Builds {@link GoogleAccountsService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactory extends AbstractServiceFactory<GoogleAccountsService> {
    
    private GoogleSaml20ObjectBuilder builder;

    private String publicKeyLocation;

    private String privateKeyLocation;

    private String keyAlgorithm;
    
    private int skewAllowance;

    /**
     * Instantiates a new Google accounts service factory.
     */
    public GoogleAccountsServiceFactory() {
    }

    @Override
    public GoogleAccountsService createService(final HttpServletRequest request) {

        final String relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);

        final String xmlRequest = this.builder.decodeSamlAuthnRequest(
                request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST));

        if (StringUtils.isBlank(xmlRequest)) {
            logger.trace("SAML AuthN request not found in the request");
            return null;
        }

        final Document document = this.builder.constructDocumentFromXml(xmlRequest);

        if (document == null) {
            return null;
        }

        final Element root = document.getRootElement();
        final String assertionConsumerServiceUrl = root.getAttributeValue("AssertionConsumerServiceURL");
        final String requestId = root.getAttributeValue("ID");

        final GoogleAccountsServiceResponseBuilder responseBuilder =
                new GoogleAccountsServiceResponseBuilder(this.privateKeyLocation, this.publicKeyLocation, this.keyAlgorithm, this.builder);
        responseBuilder.setSkewAllowance(this.skewAllowance);
        return new GoogleAccountsService(assertionConsumerServiceUrl, relayState, requestId, responseBuilder);
    }

    @Override
    public GoogleAccountsService createService(final String id) {
        throw new NotImplementedException("This operation is not supported.");
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }

    public void setPublicKeyLocation(final String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public void setPrivateKeyLocation(final String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public void setKeyAlgorithm(final String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public void setBuilder(final GoogleSaml20ObjectBuilder builder) {
        this.builder = builder;
    }
}
