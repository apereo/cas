package org.jasig.cas.authentication.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract response builder that provides wrappers for building
 * post and redirect responses.
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractWebApplicationServiceResponseBuilder implements ResponseBuilder<WebApplicationService> {
    private static final long serialVersionUID = -4584738964007702423L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Build redirect.
     *
     * @param service the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildRedirect(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getRedirectResponse(service.getOriginalUrl(), parameters);
    }


    /**
     * Build post.
     *
     * @param service the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildPost(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getPostResponse(service.getOriginalUrl(), parameters);
    }
}
