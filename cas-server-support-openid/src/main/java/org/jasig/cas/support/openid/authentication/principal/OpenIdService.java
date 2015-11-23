package org.jasig.cas.support.openid.authentication.principal;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.ResponseBuilder;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public final class OpenIdService extends AbstractWebApplicationService {

    /** The Constant LOGGER. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(OpenIdService.class);

    private static final long serialVersionUID = 5776500133123291301L;

    private final String identity;

    /**
     * Instantiates a new OpenID service.
     *
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param openIdIdentity the OpenID identity
     * @param responseBuilder the response builder
     */
    protected OpenIdService(final String id, final String originalUrl,
                            final String artifactId, final String openIdIdentity,
                            final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, originalUrl, artifactId, responseBuilder);
        this.identity = openIdIdentity;
    }


    /**
     * Return that the service is already logged out.
     *
     * @return that the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.identity)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenIdService other = (OpenIdService) obj;
        if (this.identity == null) {
            if (other.identity != null) {
                return false;
            }
        } else if (!this.identity.equals(other.identity)) {
            return false;
        }
        return true;
    }

    public String getIdentity() {
        return this.identity;
    }
}
