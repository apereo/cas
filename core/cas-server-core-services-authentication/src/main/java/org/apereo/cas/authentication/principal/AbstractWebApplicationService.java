package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.validation.ValidationResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of a WebApplicationService.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "service_type", length = 50, discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "VARCHAR(50) DEFAULT 'simple'")
@Table(name = "WebApplicationServices")
public abstract class AbstractWebApplicationService implements WebApplicationService {

    private static final long serialVersionUID = 610105280927740076L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebApplicationService.class);
    
    /**
     * The id of the service.
     */
    @Id
    @JsonProperty
    private String id;

    /**
     * The original url provided, used to reconstruct the redirect url.
     */
    @JsonProperty
    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String originalUrl;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String artifactId;

    @JsonProperty
    private String principal;

    @Column(updatable = true, insertable = true, nullable = false)
    private boolean loggedOutAlready;

    @Column(updatable = true, insertable = true, nullable = false)
    private ValidationResponseType format = ValidationResponseType.XML;

    protected AbstractWebApplicationService() {}
    
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
        return new HashMap<>(0);
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


    public String getPrincipal() {
        return this.principal;
    }

    @Override
    public void setPrincipal(final String principal) {
        this.principal = principal;
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
