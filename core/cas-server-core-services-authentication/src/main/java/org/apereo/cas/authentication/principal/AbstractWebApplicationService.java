package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.jpa.MultivaluedMapToJsonAttributeConverter;
import org.apereo.cas.validation.ValidationResponseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import java.io.Serial;
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

    @JsonProperty
    @Column
    private String tenant;

    @Column
    private boolean loggedOutAlready;

    @Column
    private ValidationResponseType format = ValidationResponseType.XML;

    @Column(columnDefinition = "json")
    @Convert(converter = MultivaluedMapToJsonAttributeConverter.class)
    private Map<String, Object> attributes = new HashMap<>();

    protected AbstractWebApplicationService(final String id, final String originalUrl, final String artifactId) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.artifactId = artifactId;
    }

    @Override
    @CanIgnoreReturnValue
    @JsonIgnore
    public WebApplicationService setFragment(final String fragment) {
        if (StringUtils.isNotBlank(fragment)) {
            this.id = collectFragmentFor(this.id, fragment);
            this.originalUrl = collectFragmentFor(this.originalUrl, fragment);
        }
        return this;
    }

    @Override
    @JsonIgnore
    public String getFragment() {
        return FunctionUtils.doAndHandle(() -> new URIBuilder(this.id).getFragment());
    }

    private static String collectFragmentFor(final String id, final String fragment) {
        return FunctionUtils.doUnchecked(() -> new URIBuilder(id)
            .setFragment(fragment)
            .build()
            .toURL()
            .toExternalForm());
    }
}
