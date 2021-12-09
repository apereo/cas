package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.support.saml.SamlIdPConstants;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * The {@link SamlIdPErrorController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("samlIdPErrorController")
public class SamlIdPErrorController {
    /**
     * Handle request model and view.
     *
     * @return the model and view
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_ERROR)
    public ModelAndView handleRequest() {
        return new ModelAndView("saml2-idp/casSamlIdPErrorView");
    }
}
