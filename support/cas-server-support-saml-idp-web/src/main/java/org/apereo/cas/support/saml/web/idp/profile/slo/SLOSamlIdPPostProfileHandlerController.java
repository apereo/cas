package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SLOSamlIdPPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "SAML2")
public class SLOSamlIdPPostProfileHandlerController extends AbstractSamlSLOProfileHandlerController {

    public SLOSamlIdPPostProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Throwable the throwable
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST)
    @Operation(summary = "Handle SAML2 SLO POST Profile Request")
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Throwable {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.POST);
        handleSloProfileRequest(response, request, decoder, SAMLConstants.SAML2_POST_BINDING_URI);
    }
}
