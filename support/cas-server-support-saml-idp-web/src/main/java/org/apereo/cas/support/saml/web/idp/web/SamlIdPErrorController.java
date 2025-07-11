package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.web.AbstractController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * The {@link SamlIdPErrorController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "SAML2")
public class SamlIdPErrorController extends AbstractController {
    /**
     * Handle request model and view.
     *
     * @return the model and view
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_ERROR)
    @Operation(summary = "Handle request model and view")
    public ModelAndView handleRequest() {
        return new ModelAndView(SamlIdPConstants.VIEW_ID_SAML_IDP_ERROR);
    }
}
