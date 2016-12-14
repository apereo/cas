package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSaml2ResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = 1488837627964481272L;

    /**
     * Build response response.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param service      the service
     * @param adaptor      the adaptor
     * @param request      the request
     * @param response     the response
     * @return the response
     * @throws SamlException the saml exception
     */
    protected Response buildResponse(final Assertion assertion,
                                     final AuthnRequest authnRequest,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response) throws SamlException {
        final String id = '_' + String.valueOf(Math.abs(new SecureRandom().nextLong()));
        Response samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());
        samlResponse.setConsent(RequestAbstractType.UNSPECIFIED_CONSENT);

        final SAMLObject finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            logger.debug("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            logger.debug("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.configBean, samlResponse);

        if (service.isSignResponses()) {
            logger.debug("SAML entity id [{}] indicates that SAML responses should be signed",
                    adaptor.getEntityId());
            samlResponse = this.samlObjectSigner.encode(samlResponse, service, adaptor, response, request);
        }

        return samlResponse;
    }
}
