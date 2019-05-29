package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is {@link SLOSamlPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SLOSamlPostProfileHandlerController extends AbstractSamlSLOProfileHandlerController {

    public SLOSamlPostProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        val decoder = getSamlProfileHandlerConfigurationContext().getSamlMessageDecoders().get(HttpMethod.POST);
        handleSloProfileRequest(response, request, decoder);
    }

    @Override
    protected void sendResponse(final HttpServletResponse response,
                                final HttpServletRequest request,
                                final LogoutRequest logoutRequest,
                                final MessageContext ctx) throws IOException {
        if (!getSamlProfileHandlerConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getLogout().isReturnLogoutResponse()) {
            super.sendResponse(response, request, logoutRequest, ctx);
            return;
        }

        val service = getSamlProfileHandlerConfigurationContext().getServicesManager().findServiceBy(SamlIdPUtils.getIssuerFromSamlObject(logoutRequest), SamlRegisteredService.class);
        val metadataResolver = getSamlProfileHandlerConfigurationContext().getSamlRegisteredServiceCachingMetadataResolver();
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(metadataResolver, service, SamlIdPUtils.getIssuerFromSamlObject(logoutRequest)).get();
        samlProfileHandlerConfigurationContext.getLogoutResponseBuilder().build(
                logoutRequest,
                request,
                response,
                service,
                adaptor,
                SAMLConstants.SAML2_POST_BINDING_URI,
                ctx
        );
    }
}
