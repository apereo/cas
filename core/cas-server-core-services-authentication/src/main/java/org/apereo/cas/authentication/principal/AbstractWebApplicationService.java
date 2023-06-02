package org.apereo.cas.authentication.principal;

import org.apereo.cas.validation.ValidationResponseType;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;

import java.io.Serial;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public abstract class AbstractWebApplicationService implements WebApplicationService {
    @Serial
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

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Map<String, List<Object>> attributes = new HashMap<>(0);

    protected AbstractWebApplicationService(final String id, final String originalUrl, final String artifactId) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.artifactId = artifactId;
    }
}
