package org.apereo.cas.support.saml.web;

import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlValidateController} is responsible for
 * validating requests based on the saml1 protocol.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlValidateController extends AbstractServiceValidateController {

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = SamlProtocolConstants.ENDPOINT_SAML_VALIDATE)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        return super.handleRequestInternal(request, response);
    }

}
