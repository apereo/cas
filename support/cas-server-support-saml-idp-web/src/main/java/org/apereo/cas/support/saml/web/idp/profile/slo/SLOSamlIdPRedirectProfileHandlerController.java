package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SLOSamlIdPRedirectProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO Redirects.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag(name = "SAML2")
public class SLOSamlIdPRedirectProfileHandlerController extends AbstractSamlSLOProfileHandlerController {
    public SLOSamlIdPRedirectProfileHandlerController(final SamlProfileHandlerConfigurationContext context) {
        super(context);
    }

    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_REDIRECT)
    @Operation(summary = "Handle SAML2 SLO Redirect Profile Request")
    protected void handleSaml2ProfileSLORedirectRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Throwable {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.GET);
        handleSloProfileRequest(response, request, decoder, SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    }
}
