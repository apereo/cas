package org.apereo.cas.authentication.principal;

import org.apereo.cas.validation.ValidationResponseType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of a WebApplicationService.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "service_type", length = 50,
    discriminatorType = DiscriminatorType.STRING, columnDefinition = "VARCHAR(50) DEFAULT 'simple'")
@Table(name = "WebApplicationServices")
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractWebApplicationService implements WebApplicationService {
    private static final long serialVersionUID = 610105280927740076L;

    @Id
    @JsonProperty
    @Column
    private String id;

    @JsonProperty
    @Column(nullable = false)
    private String originalUrl;

    @Column
    private String artifactId;

    @JsonProperty
    @Column
    private String principal;

    @JsonProperty
    @Column
    private String source;

    @Column
    private boolean loggedOutAlready;

    @Column
    private ValidationResponseType format = ValidationResponseType.XML;

    @Column
    @Lob
    private HashMap<String, List<Object>> attributes = new HashMap<>(0);

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
    public Map<String, List<Object>> getAttributes() {
        return this.attributes;
    }

}
