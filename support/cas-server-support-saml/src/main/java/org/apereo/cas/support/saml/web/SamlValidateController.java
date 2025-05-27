package org.apereo.cas.support.saml.web;

import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link SamlValidateController} is responsible for
 * validating requests based on the saml1 protocol.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag(name = "CAS")
public class SamlValidateController extends AbstractServiceValidateController {

    public SamlValidateController(final ServiceValidateConfigurationContext serviceValidateConfigurationContext) {
        super(serviceValidateConfigurationContext);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = {SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, "/tenants/{tenant}/" + SamlProtocolConstants.ENDPOINT_SAML_VALIDATE})
    @Override
    @Operation(summary = "Validate a service ticket",
        parameters = {
            @Parameter(name = "tenant", description = "The tenant definition", in = ParameterIn.PATH),
            @Parameter(name = "TARGET", description = "The service identifier"),
            @Parameter(name = "SAMLart", description = "The service ticket identifier")
        })
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                              final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

}
