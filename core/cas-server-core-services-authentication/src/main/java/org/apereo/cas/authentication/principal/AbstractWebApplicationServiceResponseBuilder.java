package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract response builder that provides wrappers for building
 * post and redirect responses.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractWebApplicationServiceResponseBuilder implements ResponseBuilder<WebApplicationService> {
    private static final long serialVersionUID = -4584738964007702423L;

    /**
     * Services manager instance.
     */
    protected final transient ServicesManager servicesManager;

    private int order;

    /**
     * Build redirect.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildRedirect(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getRedirectResponse(determineServiceResponseUrl(service), parameters);
    }

    protected String determineServiceResponseUrl(final WebApplicationService service) {
        val registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null && StringUtils.isNotBlank(registeredService.getRedirectUrl())) {
            return registeredService.getRedirectUrl();
        }
        return service.getOriginalUrl();
    }

    /**
     * Build header response.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildHeader(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getHeaderResponse(determineServiceResponseUrl(service), parameters);
    }

    /**
     * Build post.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildPost(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getPostResponse(determineServiceResponseUrl(service), parameters);
    }

    /**
     * Determine response type response.
     *
     * @param finalService the final service
     * @return the response type
     */
    protected Response.ResponseType getWebApplicationServiceResponseType(final WebApplicationService finalService) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val methodRequest = Optional.ofNullable(request)
            .map(httpServletRequest -> httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_METHOD))
            .orElse(null);
        final Function<String, String> func = FunctionUtils.doIf(StringUtils::isBlank,
            t -> {
                val registeredService = this.servicesManager.findServiceBy(finalService);
                if (registeredService != null) {
                    return registeredService.getResponseType();
                }
                return null;
            },
            f -> methodRequest);

        val method = func.apply(methodRequest);
        if (StringUtils.isBlank(method)) {
            return Response.ResponseType.REDIRECT;
        }
        return Response.ResponseType.valueOf(method.toUpperCase());
    }
}
