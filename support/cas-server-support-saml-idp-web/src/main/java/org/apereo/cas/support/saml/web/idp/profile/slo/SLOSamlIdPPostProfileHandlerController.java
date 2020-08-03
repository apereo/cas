package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SLOSamlIdPPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SLOSamlIdPPostProfileHandlerController extends AbstractSamlSLOProfileHandlerController {

    public SLOSamlIdPPostProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
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
        val decoder = getSamlProfileHandlerConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.POST);
        handleSloProfileRequest(response, request, decoder);
    }
}
