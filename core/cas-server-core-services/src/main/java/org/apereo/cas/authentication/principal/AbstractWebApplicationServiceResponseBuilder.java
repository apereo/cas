package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * Abstract response builder that provides wrappers for building
 * post and redirect responses.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractWebApplicationServiceResponseBuilder implements ResponseBuilder<WebApplicationService> {
    private static final long serialVersionUID = -4584738964007702423L;

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
     * Build post.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the response
     */
    protected Response buildPost(final WebApplicationService service, final Map<String, String> parameters) {
        return DefaultResponse.getPostResponse(service.getOriginalUrl(), parameters);
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return new EqualsBuilder().isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().toHashCode();
    }
}
