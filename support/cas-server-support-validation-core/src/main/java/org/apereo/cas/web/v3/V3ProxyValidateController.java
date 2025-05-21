package org.apereo.cas.web.v3;

import org.apereo.cas.CasProtocolConstants;
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
 * CAS proxy validation endpoint.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag(name = "CAS")
public class V3ProxyValidateController extends V3ServiceValidateController {

    public V3ProxyValidateController(final ServiceValidateConfigurationContext ctx) {
        super(ctx);
    }

    @GetMapping(path = {CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3, "/tenants/{tenant}/" + CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3})
    @Override
    @Operation(summary = "Validate a service ticket",
        parameters = {
            @Parameter(name = "tenant", description = "The tenant definition", in = ParameterIn.PATH),
            @Parameter(name = "service", description = "The service identifier"),
            @Parameter(name = "ticket", description = "The service ticket identifier")
        })
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequestInternal(request, response);
    }
}
