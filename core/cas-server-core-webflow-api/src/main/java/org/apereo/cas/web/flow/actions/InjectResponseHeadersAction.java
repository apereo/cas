package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link InjectResponseHeadersAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InjectResponseHeadersAction extends RedirectToServiceAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectResponseHeadersAction.class);

    public InjectResponseHeadersAction(final ResponseBuilderLocator responseBuilderLocator) {
        super(responseBuilderLocator);
    }

    @Override
    protected Event finalizeResponseEvent(final RequestContext requestContext,
                                          final WebApplicationService service,
                                          final Response response) {
        final HttpServletResponse httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        httpResponse.addHeader(CasProtocolConstants.PARAMETER_SERVICE, response.getUrl());
        response.getAttributes().forEach(httpResponse::addHeader);
        return success();
    }
}
