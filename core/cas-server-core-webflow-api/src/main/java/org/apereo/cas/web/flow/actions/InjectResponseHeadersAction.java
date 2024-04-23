package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.Locale;

/**
 * This is {@link InjectResponseHeadersAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InjectResponseHeadersAction extends RedirectToServiceAction {
    public InjectResponseHeadersAction(final ResponseBuilderLocator responseBuilderLocator) {
        super(responseBuilderLocator);
    }

    @Override
    protected String getFinalResponseEventId(final WebApplicationService service, final Response response, final RequestContext requestContext) {
        val httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        httpResponse.addHeader(CasProtocolConstants.PARAMETER_SERVICE, response.url());
        response.attributes().forEach(httpResponse::addHeader);
        if (response.attributes().containsKey(Response.ResponseType.REDIRECT.name().toLowerCase(Locale.ENGLISH))) {
            return CasWebflowConstants.TRANSITION_ID_REDIRECT;
        }
        return CasWebflowConstants.TRANSITION_ID_SUCCESS;
    }
}
