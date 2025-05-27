package org.apereo.cas.web.v2;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CAS v2 ticket validation endpoint.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Tag(name = "CAS")
public class ServiceValidateController extends AbstractServiceValidateController {

    public ServiceValidateController(final ServiceValidateConfigurationContext serviceValidateConfigurationContext) {
        super(serviceValidateConfigurationContext);
    }

    @GetMapping(path = { CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE, "/tenants/{tenant}/" + CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE })
    @Override
    @Operation(summary = "Validate a service ticket",
        parameters = {
            @Parameter(name = "tenant", description = "The tenant definition", in = ParameterIn.PATH),
            @Parameter(name = "service", description = "The service identifier"),
            @Parameter(name = "ticket", description = "The service ticket identifier")
        })
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected void prepareForTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
        super.prepareForTicketValidation(request, service, serviceTicketId);
        LOGGER.debug("Preparing to validate ticket [{}] for service [{}] via [{}]. Do note that this validation request "
                + "is not equipped to release principal attributes to applications. To access the authenticated "
                + "principal along with attributes, invoke the [{}] endpoint instead.",
            CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE,
            serviceTicketId, service, CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3);
    }
}
