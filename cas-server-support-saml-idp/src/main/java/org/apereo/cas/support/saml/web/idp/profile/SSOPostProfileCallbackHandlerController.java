package org.apereo.cas.support.saml.web.idp.profile;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.CasProtocolConstants;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SSOPostProfileCallbackHandlerController}, which handles
 * the profile callback request to build the final saml response.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SSOPostProfileCallbackHandlerController extends AbstractSamlProfileHandlerController {

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK, method = RequestMethod.GET)
    protected void handleCallbackProfileRequest(final HttpServletResponse response,
                                                final HttpServletRequest request) throws Exception {

        logger.info("Received SAML callback profile request [{}]", request.getRequestURI());
        final AuthnRequest authnRequest = retrieveAuthnRequest(request);
        if (authnRequest == null) {
            logger.error("Can not validate the request because the original Authn request can not be found.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            logger.error("Can not validate the request because no [{}] is provided via the request",
                    CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(getServerPrefix());
        validator.setRenew(authnRequest.isForceAuthn());
        final String serviceUrl = constructServiceUrl(request, response, authnRequest);
        logger.debug("Created service url for validation: [{}]", serviceUrl);
        final Assertion assertion = validator.validate(ticket, serviceUrl);
        Thread.sleep(1);
        logCasValidationAssertion(assertion);

        if (!assertion.isValid()) {
            throw new SamlException("CAS assertion received is invalid. This normally indicates that the assertion received has expired "
                    + " and is not valid within the time constraints of the authentication event");
        }
        final String issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
        final SamlRegisteredService registeredService = verifySamlRegisteredService(issuer);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor = getSamlMetadataFacadeFor(registeredService, authnRequest);

        logger.debug("Preparing SAML response for [{}]", adaptor.getEntityId());
        this.responseBuilder.build(authnRequest, request, response, assertion, registeredService, adaptor);
        logger.info("Built the SAML response for [{}]", adaptor.getEntityId());

        
    }

}
