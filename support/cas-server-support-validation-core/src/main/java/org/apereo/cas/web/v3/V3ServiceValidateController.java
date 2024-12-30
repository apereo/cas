package org.apereo.cas.web.v3;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CAS v3 service ticket validation endpoint.
 * 
 * @author Misagh Moayyed
 * @since 4.2
 */
public class V3ServiceValidateController extends AbstractServiceValidateController {

    public V3ServiceValidateController(final ServiceValidateConfigurationContext ctx) {
        super(ctx);
    }

    @GetMapping(path = { CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3, "/tenants/{tenant}/" + CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3 })
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequestInternal(request, response);
    }
}
