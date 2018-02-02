package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.validation.ValidationResponseType;

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
@DiscriminatorColumn(name = "service_type", length = 50, discriminatorType = DiscriminatorType.STRING, columnDefinition = "VARCHAR(50) DEFAULT 'simple'")
@Table(name = "WebApplicationServices")
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractWebApplicationService implements WebApplicationService {
    private static final long serialVersionUID = 610105280927740076L;

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
    @Column(nullable = false)
    private String originalUrl;

    @Column
    private String artifactId;

    @JsonProperty
    private String principal;

    @Column
    private boolean loggedOutAlready;

    @Column
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

    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(0);
    }

}
