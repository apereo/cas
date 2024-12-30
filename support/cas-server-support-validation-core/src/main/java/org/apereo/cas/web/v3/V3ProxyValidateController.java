package org.apereo.cas.web.v3;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
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
public class V3ProxyValidateController extends V3ServiceValidateController {

    public V3ProxyValidateController(final ServiceValidateConfigurationContext ctx) {
        super(ctx);
    }

    @GetMapping(path = {CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3, "/tenants/{tenant}/" + CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3})
    @Override
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequestInternal(request, response);
    }
}
