package org.apereo.cas.authentication.principal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Abstract response builder that provides wrappers for building
 * post and redirect responses.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractWebApplicationServiceResponseBuilder implements ResponseBuilder<WebApplicationService> {
    private static final long serialVersionUID = -4584738964007702423L;

    /**
     * Services manager instance.
     */
    protected final ServicesManager servicesManager;

    private int order;

    /**
     * Build redirect.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildRedirect(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getRedirectResponse(service.getOriginalUrl(), parameters);
    }

    /**
     * Build header response.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildHeader(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getHeaderResponse(service.getOriginalUrl(), parameters);
    }


    /**
     * Build post.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildPost(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getPostResponse(service.getOriginalUrl(), parameters);
    }

    /**
     * Determine response type response.
     *
     * @param finalService the final service
     * @return the response type
     */
    protected Response.ResponseType getWebApplicationServiceResponseType(final WebApplicationService finalService) {
        final HttpServletRequest request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        String method = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_METHOD) : null;
        if (StringUtils.isBlank(method)) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(finalService);
            if (registeredService != null) {
                method = registeredService.getResponseType();
            }
        }

        if (StringUtils.isBlank(method)) {
            return Response.ResponseType.REDIRECT;
        }

        if (StringUtils.equalsIgnoreCase(method, Response.ResponseType.HEADER.name())) {
            return Response.ResponseType.HEADER;
        }
        if (StringUtils.equalsIgnoreCase(method, Response.ResponseType.POST.name())) {
            return Response.ResponseType.POST;
        }

        return Response.ResponseType.REDIRECT;
    }
}
