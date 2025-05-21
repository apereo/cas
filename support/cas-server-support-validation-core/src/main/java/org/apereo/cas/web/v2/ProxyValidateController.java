package org.apereo.cas.web.v2;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Proxy validation controller.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag(name = "CAS")
public class ProxyValidateController extends AbstractServiceValidateController {

    public ProxyValidateController(final ServiceValidateConfigurationContext serviceValidateConfigurationContext) {
        super(serviceValidateConfigurationContext);
    }

    @GetMapping(path = { CasProtocolConstants.ENDPOINT_PROXY_VALIDATE, "/tenants/{tenant}/" + CasProtocolConstants.ENDPOINT_PROXY_VALIDATE })
    @Override
    @Operation(summary = "Validate a service ticket",
        parameters = {
            @Parameter(name = "tenant", description = "The tenant definition", in = ParameterIn.PATH),
            @Parameter(name = "service", description = "The service identifier"),
            @Parameter(name = "ticket", description = "The service ticket identifier")
        })
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                              final HttpServletResponse response) throws Exception {
        return getServiceValidateConfigurationContext().getCasProperties().getSso().isProxyAuthnEnabled()
            ? super.handleRequestInternal(request, response)
            : null;
    }
}
