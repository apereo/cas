package org.jasig.cas.support.saml.web.idp.profile;

import org.jasig.cas.support.saml.SamlIdPConstants;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SLOPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Controller("sloPostProfileHandlerController")
public class SLOPostProfileHandlerController extends AbstractSamlProfileHandlerController {
    /**
     * Instantiates a new Slo post profile handler controller.
     */
    public SLOPostProfileHandlerController() {
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        handleSloPostProfileRequest(response, request, new HTTPPostDecoder());
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleSloPostProfileRequest(final HttpServletResponse response,
                                               final HttpServletRequest request,
                                               final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        final LogoutRequest logoutRequest = decodeRequest(request, decoder, LogoutRequest.class);
        final SamlRegisteredService registeredService = verifySamlRegisteredService(logoutRequest.getDestination());
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                        registeredService, logoutRequest);
        SamlIdPUtils.logSamlObject(this.configBean, logoutRequest);

        if (!logoutRequest.isSigned()) {
            throw new SAMLException("Logout request is not signed but should be");
        } else {
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(logoutRequest, registeredService, adaptor);
        }
        
    }
}
