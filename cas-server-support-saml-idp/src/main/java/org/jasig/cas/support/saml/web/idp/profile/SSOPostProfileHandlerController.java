package org.jasig.cas.support.saml.web.idp.profile;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SSOPostProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Controller("ssoPostProfileHandlerController")
public class SSOPostProfileHandlerController extends AbstractSamlProfileHandlerController {

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private SamlProfileSamlResponseBuilder responseBuilder;

    /**
     * Instantiates a new redirect profile handler controller.
     */
    public SSOPostProfileHandlerController() {}

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK, method = RequestMethod.GET)
    protected void handleCallbackProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request) throws Exception {
        try {
            logger.info("Received SAML callback profile request {}", request.getRequestURI());
            final AuthnRequest authnRequest = retrieveAuthnRequest(request);
            if (authnRequest == null) {
                logger.error("Can not validate the request because the original Authn request can not be found.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
            if (StringUtils.isBlank(ticket)) {
                logger.error("Can not validate the request because no {} is provided via the request",
                        CasProtocolConstants.PARAMETER_TICKET);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(this.casServerPrefix);
            validator.setRenew(authnRequest.isForceAuthn());
            final String serviceUrl = constructServiceUrl(request, response, this.callbackService.getId(), this.casServerName);
            logger.debug("Created service url for validation: {}", serviceUrl);
            final Assertion assertion = validator.validate(ticket, serviceUrl);
            logCasValidationAssertion(assertion);
            if (!assertion.isValid()) {
                throw new SamlException("CAS assertion received is invalid");
            }
            final SamlRegisteredService registeredService = verifySamlRegisteredService(authnRequest);
            final SamlMetadataAdaptor adaptor = SamlMetadataAdaptor.adapt(registeredService, authnRequest);

            logger.debug("Preparing SAML response for {}", adaptor.getEntityId());
            responseBuilder.build(authnRequest, request, response, assertion, registeredService, adaptor);
            logger.info("Built the SAML response for {}", adaptor.getEntityId());

        } finally {
            storeAuthnRequest(request, null);
        }
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request the request
     * @throws Exception the exception
     */
    @RequestMapping(path= SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT, method = RequestMethod.GET)
    protected void handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleProfileRequest(response, request, new HTTPRedirectDeflateDecoder());
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request the request
     * @throws Exception the exception
     */
    @RequestMapping(path= SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleProfileRequest(response, request, new HTTPPostDecoder());
    }

}
