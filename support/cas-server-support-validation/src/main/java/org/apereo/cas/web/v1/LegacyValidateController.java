package org.apereo.cas.web.v1;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class LegacyValidateController extends AbstractServiceValidateController {
    public LegacyValidateController(final ServiceValidateConfigurationContext serviceValidateConfigurationContext) {
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
    @GetMapping(path = CasProtocolConstants.ENDPOINT_VALIDATE)
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected void prepareForTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
        super.prepareForTicketValidation(request, service, serviceTicketId);
        LOGGER.debug("Preparing to validate ticket [{}] for service [{}] via [{}]. Do note that this validation event "
                + "is not equipped to release principal attributes to applications. To access the authenticated "
                + "principal along with attributes, invoke the [{}] endpoint instead.",
            CasProtocolConstants.ENDPOINT_VALIDATE,
            serviceTicketId, service, CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3);
    }
}
