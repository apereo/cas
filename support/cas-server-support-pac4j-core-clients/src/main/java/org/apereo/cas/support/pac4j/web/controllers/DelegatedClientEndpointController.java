package org.apereo.cas.support.pac4j.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * Processes delegation response over /delegatedAuthn/ context path
 * <p>
 * This controller extracts the client name from the context path, enriches the request by client_name attribute
 * and forwards it for further processing by the webflow for /login?client_name </p>
 *
 * @see org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction
 *
 * @author Ghenadii Batalski
 * @since 5.2.0
 */
@Controller
public class DelegatedClientEndpointController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClientEndpointController.class);

   @RequestMapping("/delegatedAuthn/{clientType}/{clientName}")
    public ModelAndView delegateAuthnClient(@PathVariable String clientType, @PathVariable String clientName, ModelMap model) {
       return new ModelAndView("forward:/login?client_name="+clientName, model);
    }
}
