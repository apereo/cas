package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.beans.factory.annotation.Value;
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
 * @since 5.0.0
 */
@Controller("sloPostProfileHandlerController")
public class SLOPostProfileHandlerController extends AbstractSamlProfileHandlerController {

    @Value("${cas.samlidp.logout.request.force.signed:true}")
    private boolean forceSignedLogoutRequests = true;

    @Value("${cas.samlidp.logout.slo.callbacks.disabled:false}")
    private boolean singleLogoutCallbacksDisabled;

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
        if (this.singleLogoutCallbacksDisabled) {
            logger.info("Processing SAML IdP SLO requests is disabled");
            return;
        }

        final LogoutRequest logoutRequest = decodeRequest(request, decoder, LogoutRequest.class);
        if (this.forceSignedLogoutRequests && !logoutRequest.isSigned()) {
            throw new SAMLException("Logout request is not signed but should be.");
        } else if (logoutRequest.isSigned()) {
            final MetadataResolver resolver = SamlIdPUtils.getMetadataResolverForAllSamlServices(this.servicesManager,
                    SamlIdPUtils.getIssuerFromSamlRequest(logoutRequest),
                    this.samlRegisteredServiceCachingMetadataResolver);
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(logoutRequest, resolver);
        }
        SamlUtils.logSamlObject(this.configBean, logoutRequest);
        response.sendRedirect(this.casServerPrefix.concat("/logout"));
    }

}
