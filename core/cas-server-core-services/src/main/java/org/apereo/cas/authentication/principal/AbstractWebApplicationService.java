package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.validation.ValidationResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Abstract implementation of a WebApplicationService.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public abstract class AbstractWebApplicationService implements WebApplicationService {

    private static final long serialVersionUID = 610105280927740076L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebApplicationService.class);
    
    /**
     * The id of the service.
     */
    @JsonProperty
    private String id;

    /**
     * The original url provided, used to reconstruct the redirect url.
     */
    @JsonProperty
    private String originalUrl;

    private String artifactId;

    @JsonProperty
    private Principal principal;

    private boolean loggedOutAlready;

    private ValidationResponseType format = ValidationResponseType.XML;

    /**
     * Instantiates a new abstract web application service.
     *
     * @param id          the id
     * @param originalUrl the original url
     * @param artifactId  the artifact id
     */
    protected AbstractWebApplicationService(final String id, final String originalUrl, final String artifactId) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.artifactId = artifactId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    /**
     * Return the original url provided (as {@code service} or {@code targetService} request parameter).
     * Used to reconstruct the redirect url.
     *
     * @return the original url provided.
     */
    @Override
    public String getOriginalUrl() {
        return this.originalUrl;
    }


    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public void setPrincipal(final Principal principal) {
        this.principal = principal;
    }

    @Override
    public boolean matches(final Service service) {
        try {
            final String thisUrl = URLDecoder.decode(this.id, StandardCharsets.UTF_8.name());
            final String serviceUrl = URLDecoder.decode(service.getId(), StandardCharsets.UTF_8.name());

            LOGGER.trace("Decoded urls and comparing [{}] with [{}]", thisUrl, serviceUrl);
            return thisUrl.equalsIgnoreCase(serviceUrl);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Return if the service is already logged out.
     *
     * @return if the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return this.loggedOutAlready;
    }

    /**
     * Set if the service is already logged out.
     *
     * @param loggedOutAlready if the service is already logged out.
     */
    @Override
    public void setLoggedOutAlready(final boolean loggedOutAlready) {
        this.loggedOutAlready = loggedOutAlready;
    }

    @JsonIgnore
    @Override
    public ValidationResponseType getFormat() {
        return this.format;
    }

    public void setFormat(final ValidationResponseType format) {
        this.format = format;
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
        final AbstractWebApplicationService rhs = (AbstractWebApplicationService) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder
                .append(this.id, rhs.id)
                .append(this.originalUrl, rhs.originalUrl)
                .append(this.artifactId, rhs.artifactId)
                .append(this.principal, rhs.principal)
                .append(this.loggedOutAlready, rhs.loggedOutAlready)
                .append(this.format, rhs.format);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(originalUrl)
                .append(artifactId)
                .append(principal)
                .append(loggedOutAlready)
                .append(format)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("originalUrl", originalUrl)
                .append("artifactId", artifactId)
                .append("principal", principal)
                .append("loggedOutAlready", loggedOutAlready)
                .append("format", format)
                .toString();
    }
}
