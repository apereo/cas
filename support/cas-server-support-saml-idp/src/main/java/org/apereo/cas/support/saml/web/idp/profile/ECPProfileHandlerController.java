package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ECPProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ECPProfileHandlerController extends AbstractSamlProfileHandlerController {
    /**
     * Handle ecp request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO, consumes = MediaType.TEXT_HTML_VALUE)
    public void handleEcpRequest(final HttpServletResponse response,
                                    final HttpServletRequest request) throws Exception {
        System.out.println();
    }
}
